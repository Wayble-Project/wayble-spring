package com.wayble.server.explore.repository.search;

import co.elastic.clients.elasticsearch._types.GeoLocation;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import com.wayble.server.explore.dto.search.request.WaybleZoneSearchConditionDto;
import com.wayble.server.explore.dto.search.response.WaybleZoneSearchResponseDto;
import com.wayble.server.explore.dto.search.response.WaybleZoneDistrictResponseDto;
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
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

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

    public List<WaybleZoneDistrictResponseDto> findTop3SearchesWaybleZonesByDistrict(String district) {
        // DEBUG: 모든 zone의 district 확인
        debugAllZoneDistricts();
        
        // 1. 특정 district에 속한 wayble zone들 조회
        List<Long> zoneIdsInDistrict = getZoneIdsByDistrict(district);
        
        if (zoneIdsInDistrict.isEmpty()) {
            System.out.println("DEBUG: No zones found for district: " + district);
            return Collections.emptyList();
        }

        // 2. 해당 zone들의 방문 로그 수 집계 및 top3 선택
        Map<Long, Long> visitCounts = getVisitCountsByZoneIds(zoneIdsInDistrict);
        
        if (visitCounts.isEmpty()) {
            System.out.println("DEBUG: No visit counts found");
            return Collections.emptyList();
        }
        
        Map<Long, Long> top3VisitCounts = visitCounts.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(DISTRICT_SEARCH_SIZE)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
        
        System.out.println("DEBUG: Top3 visit counts: " + top3VisitCounts);

        // 3. top3 zone들의 상세 정보 조회 및 ResponseDto 생성
        return getWaybleZoneDetails(new ArrayList<>(top3VisitCounts.keySet()), top3VisitCounts);
    }

    private List<Long> getZoneIdsByDistrict(String district) {
        System.out.println("=== DEBUG: Searching for district: " + district + " ===");
        
        Query termQuery = TermQuery.of(t -> t
                .field("address.district.keyword")
                .value(district)
        )._toQuery();

        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(termQuery)
                .withMaxResults(1000) // district 내 모든 zone 조회
                .build();

        SearchHits<WaybleZoneDocument> searchHits = operations.search(
                searchQuery,
                WaybleZoneDocument.class,
                INDEX
        );

        System.out.println("DEBUG: Found " + searchHits.getTotalHits() + " zones in district: " + district);
        
        // 첫 번째 결과 몇 개의 district 값 확인
        searchHits.getSearchHits().stream().limit(3).forEach(hit -> {
            WaybleZoneDocument doc = hit.getContent();
            System.out.println("DEBUG: Zone " + doc.getZoneId() + " has district: [" + doc.getAddress().getDistrict() + "]");
        });

        return searchHits.getSearchHits().stream()
                .map(hit -> hit.getContent().getZoneId())
                .collect(Collectors.toList());
    }


    private Map<Long, Long> getVisitCountsByZoneIds(List<Long> zoneIds) {
        Map<Long, Long> visitCountMap = new HashMap<>();
        
        System.out.println("DEBUG: Checking visit counts for " + zoneIds.size() + " zones");
        
        // 각 zoneId별로 방문 로그 수를 직접 카운트
        for (Long zoneId : zoneIds) {
            long count = countVisitLogsByZoneId(zoneId);
            visitCountMap.put(zoneId, count);
            System.out.println("DEBUG: Zone " + zoneId + " has " + count + " visits");
        }

        System.out.println("DEBUG: Visit count map size: " + visitCountMap.size());
        return visitCountMap;
    }

    private long countVisitLogsByZoneId(Long zoneId) {
        Query termQuery = TermQuery.of(t -> t
                .field("zoneId")
                .value(zoneId)
        )._toQuery();

        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(termQuery)
                .withMaxResults(0) // 카운트만 필요하므로 결과는 0개
                .build();

        SearchHits<?> searchHits = operations.search(
                searchQuery,
                Object.class,
                IndexCoordinates.of("wayble_zone_visit_log")
        );

        return searchHits.getTotalHits();
    }

    private List<WaybleZoneDistrictResponseDto> getWaybleZoneDetails(List<Long> zoneIds, Map<Long, Long> visitCountMap) {
        if (zoneIds.isEmpty()) {
            System.out.println("DEBUG: getWaybleZoneDetails - zoneIds is empty");
            return Collections.emptyList();
        }

        System.out.println("DEBUG: getWaybleZoneDetails - Looking for zoneIds: " + zoneIds);

        // 단일 zoneId로 먼저 테스트해보기
        Long firstZoneId = zoneIds.get(0);
        System.out.println("DEBUG: Testing single zoneId: " + firstZoneId);
        
        Query singleTermQuery = TermQuery.of(t -> t
                .field("id")
                .value(firstZoneId)
        )._toQuery();
        
        NativeQuery testQuery = NativeQuery.builder()
                .withQuery(singleTermQuery)
                .withMaxResults(1)
                .build();
                
        SearchHits<WaybleZoneDocument> testHits = operations.search(
                testQuery,
                WaybleZoneDocument.class,
                INDEX
        );
        
        System.out.println("DEBUG: Single zoneId test found: " + testHits.getTotalHits() + " zones");

        // Terms query로 변경 - 여러 값을 한번에 검색
        Query termsQuery = Query.of(q -> q
                .terms(t -> t
                        .field("id")
                        .terms(tv -> tv.value(zoneIds.stream()
                                .map(id -> co.elastic.clients.elasticsearch._types.FieldValue.of(id))
                                .collect(Collectors.toList())
                        ))
                )
        );

        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(termsQuery)
                .withMaxResults(zoneIds.size())
                .build();

        SearchHits<WaybleZoneDocument> searchHits = operations.search(
                searchQuery,
                WaybleZoneDocument.class,
                INDEX
        );

        System.out.println("DEBUG: getWaybleZoneDetails - Found " + searchHits.getTotalHits() + " zones");

        List<WaybleZoneDistrictResponseDto> result = searchHits.getSearchHits().stream()
                .map(hit -> {
                    WaybleZoneDocument doc = hit.getContent();
                    Long visitCount = visitCountMap.get(doc.getZoneId());
                    System.out.println("DEBUG: Creating DTO for zone " + doc.getZoneId() + " with " + visitCount + " visits");
                    return WaybleZoneDistrictResponseDto.from(doc, visitCount);
                })
                .sorted((a, b) -> Long.compare(b.visitCount(), a.visitCount())) // 방문 수 내림차순 정렬
                .collect(Collectors.toList());

        System.out.println("DEBUG: getWaybleZoneDetails - Returning " + result.size() + " results");
        return result;
    }
    
    private void debugAllZoneDistricts() {
        System.out.println("=== DEBUG: All zones in index ===");
        
        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(Query.of(q -> q.matchAll(m -> m)))
                .withMaxResults(20)
                .build();

        SearchHits<WaybleZoneDocument> searchHits = operations.search(
                searchQuery,
                WaybleZoneDocument.class,
                INDEX
        );
        
        System.out.println("DEBUG: Total zones in index: " + searchHits.getTotalHits());
        
        searchHits.getSearchHits().forEach(hit -> {
            WaybleZoneDocument doc = hit.getContent();
            System.out.println("DEBUG: Zone " + doc.getZoneId() + " district: [" + doc.getAddress().getDistrict() + "]");
        });
    }
}