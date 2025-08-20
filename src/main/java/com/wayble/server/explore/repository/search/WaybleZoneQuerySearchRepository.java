package com.wayble.server.explore.repository.search;

import co.elastic.clients.elasticsearch._types.GeoLocation;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.wayble.server.explore.dto.search.request.WaybleZoneSearchConditionDto;
import com.wayble.server.explore.dto.search.response.WaybleZoneSearchResponseDto;
import com.wayble.server.explore.entity.WaybleZoneDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class WaybleZoneQuerySearchRepository{

    private final ElasticsearchOperations operations;

    private static final IndexCoordinates INDEX = IndexCoordinates.of("wayble_zone");

    private static final int DISTRICT_SEARCH_SIZE = 3;

    public Slice<WaybleZoneSearchResponseDto> searchWaybleZonesByCondition(WaybleZoneSearchConditionDto cond, Pageable pageable) {

        int fetchSize = pageable.getPageSize() + 1;

        double radius = cond.radiusKm() != null ? cond.radiusKm() : 50.0;
        String radiusWithUnit = radius + "km"; // The new client often uses string representation for distance

        // 필터 및 조건 정의
        Query query = Query.of(q -> q
                .bool(b -> {
                    // zoneType이 존재하면 must 조건으로 추가
                    if (cond.zoneType() != null) {
                        b.must(m -> m
                                .term(t -> t
                                        .field("zoneType.keyword")
                                        .value(cond.zoneType().name())
                                )
                        );
                    }
                    // zoneName이 비어있지 않으면 match 조건으로 추가
                    if (cond.zoneName() != null && !cond.zoneName().isBlank()) {
                        b.must(m -> m
                                .match(mp -> mp
                                        .field("zoneName")
                                        .query(cond.zoneName())
                                )
                        );
                    }
                    // 위치 기반 필터 조건: 중심 좌표 기준 반경 필터링
                    b.filter(f -> f
                            .geoDistance(gd -> gd
                                    .field("address.location")
                                    .location(loc -> loc
                                            .latlon(ll -> ll
                                                    .lat(cond.latitude())
                                                    .lon(cond.longitude())
                                            )
                                    )
                                    .distance(radiusWithUnit)
                            )
                    );
                    return b;
                })
        );

        // 정렬 옵션 설정: 거리 기준 오름차순 정렬
        SortOptions geoSort = SortOptions.of(s -> s
                .geoDistance(gds -> gds
                        .field("address.location")
                        .location(GeoLocation.of(gl -> gl
                                .latlon(ll -> ll
                                        .lat(cond.latitude())
                                        .lon(cond.longitude())
                                )
                        ))
                        .order(SortOrder.Asc)
                )
        );

        // NativeQuery 구성: 쿼리 + 정렬 + 페이징 정보 포함
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(query)
                .withSort(geoSort)
                .withPageable(PageRequest.of(
                        pageable.getPageNumber(),
                        fetchSize   // 다음 페이지 유무를 판단하기 위해 +1 해서 조회
                ))
                .build();

        // 실제 검색 수행
        SearchHits<WaybleZoneDocument> hits =
                operations.search(nativeQuery, WaybleZoneDocument.class, INDEX);

        // 검색 결과를 DTO로 매핑
        List<WaybleZoneSearchResponseDto> dtos = hits.stream()
                .map(hit -> {
                    WaybleZoneDocument doc = hit.getContent();
                    // The distance value is returned in meters by default when sorting.
                    // We convert it to kilometers for consistency.
                    Double distanceInMeters = (Double) hit.getSortValues().get(0);
                    Double distanceInKm = distanceInMeters / 1000.0;
                    return WaybleZoneSearchResponseDto.from(doc, distanceInKm);
                })
                .toList();

        // 다음 페이지가 존재하는지 여부 판단
        boolean hasNext = dtos.size() > pageable.getPageSize();
        if (hasNext) {
            dtos = dtos.subList(0, pageable.getPageSize());
        }

        return new SliceImpl<>(dtos, pageable, hasNext);
    }

    /**
     * 30m 이내이고 이름이 유사한 WaybleZone 찾기 (최적화된 버전)
     * @param cond 검색 조건 (위도, 경도, 이름 포함)
     * @return 조건에 맞는 첫 번째 결과 또는 null
     */
    public WaybleZoneSearchResponseDto findSimilarWaybleZone(WaybleZoneSearchConditionDto cond) {
        if (cond.zoneName() == null || cond.zoneName().isBlank()) {
            return null;
        }

        // Step 1: 30m 이내 모든 후보 조회 (지리적 필터만)
        List<WaybleZoneDocument> candidates = findNearbyZones(cond);

        // Step 2: 메모리에서 텍스트 유사도 검사
        return candidates.stream()
                .filter(zone -> isTextSimilar(zone.getZoneName(), cond.zoneName()))
                .findFirst()
                .map(doc -> WaybleZoneSearchResponseDto.from(doc, null))
                .orElse(null);
    }

    /**
     * 30m 이내 모든 WaybleZone 후보 조회
     */
    private List<WaybleZoneDocument> findNearbyZones(WaybleZoneSearchConditionDto cond) {
        Query geoQuery = Query.of(q -> q
                .geoDistance(gd -> gd
                        .field("address.location")
                        .location(loc -> loc.latlon(ll -> ll
                                .lat(cond.latitude())
                                .lon(cond.longitude())
                        ))
                        .distance("30m")
                )
        );

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(geoQuery)
                .withPageable(PageRequest.of(0, 10)) // 30m 이내는 보통 10개 미만
                .build();

        SearchHits<WaybleZoneDocument> hits =
                operations.search(nativeQuery, WaybleZoneDocument.class, INDEX);

        return hits.stream()
                .map(hit -> hit.getContent())
                .toList();
    }

    /**
     * 텍스트 유사도 검사 (메모리 기반)
     */
    private boolean isTextSimilar(String zoneName, String searchName) {
        if (zoneName == null || searchName == null) {
            return false;
        }

        String normalizedZone = normalize(zoneName);
        String normalizedSearch = normalize(searchName);

        // 1. 완전 일치
        if (normalizedZone.equals(normalizedSearch)) {
            return true;
        }

        // 2. 포함 관계 (기존 wildcard와 유사)
        if (normalizedZone.contains(normalizedSearch) ||
            normalizedSearch.contains(normalizedZone)) {
            return true;
        }

        // 3. 편집 거리 (기존 fuzzy와 유사) - 70% 이상 유사
        if (calculateLevenshteinSimilarity(normalizedZone, normalizedSearch) > 0.7) {
            return true;
        }

        // 4. 자카드 유사도 (토큰 기반, 기존 match와 유사) - 60% 이상 유사
        return calculateJaccardSimilarity(normalizedZone, normalizedSearch) > 0.6;
    }

    /**
     * 텍스트 정규화 (공백, 특수문자 제거)
     */
    private String normalize(String text) {
        return text.replaceAll("\\s+", "")           // 공백 제거
                  .replaceAll("[^가-힣a-zA-Z0-9]", "") // 특수문자 제거
                  .toLowerCase();
    }

    /**
     * 레벤슈타인 거리 기반 유사도 (0.0 ~ 1.0)
     */
    private double calculateLevenshteinSimilarity(String s1, String s2) {
        if (s1.isEmpty() || s2.isEmpty()) {
            return 0.0;
        }

        int distance = levenshteinDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());
        return 1.0 - (double) distance / maxLength;
    }

    /**
     * 레벤슈타인 거리 계산
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]);
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }

    /**
     * 자카드 유사도 (문자 집합 기반, 0.0 ~ 1.0)
     */
    private double calculateJaccardSimilarity(String s1, String s2) {
        if (s1.isEmpty() && s2.isEmpty()) {
            return 1.0;
        }
        if (s1.isEmpty() || s2.isEmpty()) {
            return 0.0;
        }

        Set<Character> set1 = s1.chars().mapToObj(c -> (char) c).collect(java.util.stream.Collectors.toSet());
        Set<Character> set2 = s2.chars().mapToObj(c -> (char) c).collect(java.util.stream.Collectors.toSet());

        Set<Character> intersection = new java.util.HashSet<>(set1);
        intersection.retainAll(set2);

        Set<Character> union = new java.util.HashSet<>(set1);
        union.addAll(set2);

        return (double) intersection.size() / union.size();
    }
}