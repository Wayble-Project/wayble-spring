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
     * 30m 이내이고 이름이 유사한 WaybleZone 찾기
     * @param cond 검색 조건 (위도, 경도, 이름 포함)
     * @return 조건에 맞는 첫 번째 결과 또는 null
     */
    public WaybleZoneSearchResponseDto findSimilarWaybleZone(WaybleZoneSearchConditionDto cond) {
        if (cond.zoneName() == null || cond.zoneName().isBlank()) {
            return null;
        }

        // 30m 이내 검색
        Query query = Query.of(q -> q
                .bool(b -> {
                    // 이름 유사도 검색 (fuzzy + match 조합)
                    b.should(s -> s
                            .match(m -> m
                                    .field("zoneName")
                                    .query(cond.zoneName())
                                    .boost(2.0f) // 정확한 매치에 높은 점수
                            )
                    );
                    b.should(s -> s
                            .fuzzy(f -> f
                                    .field("zoneName")
                                    .value(cond.zoneName())
                                    .fuzziness("AUTO") // 오타 허용
                                    .boost(1.5f)
                            )
                    );
                    // 부분 매치도 포함 (공백 제거 후 검색)
                    String cleanedName = cond.zoneName().replaceAll("\\s+", "");
                    b.should(s -> s
                            .wildcard(w -> w
                                    .field("zoneName")
                                    .value("*" + cleanedName + "*")
                                    .boost(1.0f)
                            )
                    );
                    
                    // 최소 하나의 should 조건은 만족해야 함
                    b.minimumShouldMatch("1");
                    
                    // 30m 이내 필터
                    b.filter(f -> f
                            .geoDistance(gd -> gd
                                    .field("address.location")
                                    .location(loc -> loc
                                            .latlon(ll -> ll
                                                    .lat(cond.latitude())
                                                    .lon(cond.longitude())
                                            )
                                    )
                                    .distance("30m")
                            )
                    );
                    return b;
                })
        );

        // 정렬: 점수 + 거리 조합
        SortOptions scoreSort = SortOptions.of(s -> s.score(sc -> sc.order(SortOrder.Desc)));
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

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(query)
                .withSort(scoreSort)
                .withSort(geoSort)
                .withPageable(PageRequest.of(0, 1)) // 첫 번째 결과만
                .build();

        SearchHits<WaybleZoneDocument> hits =
                operations.search(nativeQuery, WaybleZoneDocument.class, INDEX);

        if (hits.isEmpty()) {
            return null;
        }

        WaybleZoneDocument doc = hits.getSearchHit(0).getContent();
        Double distanceInMeters = (Double) hits.getSearchHit(0).getSortValues().get(1); // 거리는 두 번째 정렬값
        Double distanceInKm = distanceInMeters / 1000.0;
        
        return WaybleZoneSearchResponseDto.from(doc, distanceInKm);
    }
}