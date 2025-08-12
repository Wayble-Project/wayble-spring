package com.wayble.server.explore.repository.facility;

import co.elastic.clients.elasticsearch._types.GeoLocation;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.wayble.server.explore.dto.facility.WaybleFacilityConditionDto;
import com.wayble.server.explore.dto.facility.WaybleFacilityResponseDto;
import com.wayble.server.explore.entity.WaybleFacilityDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class WaybleFacilityQuerySearchRepository {

    private final ElasticsearchOperations operations;
    private static final IndexCoordinates INDEX = IndexCoordinates.of("wayble_facility_document");
    private static final int LIMIT = 50;

    /**
     * 위도, 경도, 시설 타입을 바탕으로 WaybleFacilityDocument를 거리순으로 N개 반환
     */
    public List<WaybleFacilityResponseDto> findNearbyFacilitiesByType(
            WaybleFacilityConditionDto condition) {
        
        double radius = 10.0; // 기본 반경 5km
        String radiusWithUnit = radius + "km";

        // 시설 타입에 따른 쿼리 조건 생성
        Query query = Query.of(q -> q
                .bool(b -> {
                    // 시설 타입 조건 추가
                    if (condition.facilityType() != null) {
                        b.must(m -> m
                                .term(t -> t
                                        .field("facilityType.keyword")
                                        .value(condition.facilityType().name())
                                )
                        );
                    }
                    
                    // 위치 기반 필터: 중심 좌표 기준 반경 필터링
                    b.filter(f -> f
                            .geoDistance(gd -> gd
                                    .field("location")
                                    .location(loc -> loc
                                            .latlon(ll -> ll
                                                    .lat(condition.latitude())
                                                    .lon(condition.longitude())
                                            )
                                    )
                                    .distance(radiusWithUnit)
                            )
                    );
                    
                    return b;
                })
        );

        // 거리 기준 오름차순 정렬
        SortOptions geoSort = SortOptions.of(s -> s
                .geoDistance(gds -> gds
                        .field("location")
                        .location(GeoLocation.of(gl -> gl
                                .latlon(ll -> ll
                                        .lat(condition.latitude())
                                        .lon(condition.longitude())
                                )
                        ))
                        .order(SortOrder.Asc)
                )
        );

        // Elasticsearch 쿼리 구성
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(query)
                .withSort(geoSort)
                .withPageable(PageRequest.of(0, LIMIT))
                .build();

        // 검색 수행
        SearchHits<WaybleFacilityDocument> hits = 
                operations.search(nativeQuery, WaybleFacilityDocument.class, INDEX);

        // 결과를 Document 리스트로 반환
        return hits.stream()
                .map(hit -> {
                    WaybleFacilityDocument doc = hit.getContent();
                    return WaybleFacilityResponseDto.from(doc);
                })
                .toList();
    }
}
