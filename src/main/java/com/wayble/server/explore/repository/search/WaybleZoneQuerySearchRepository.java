package com.wayble.server.explore.repository.search;

import co.elastic.clients.elasticsearch._types.GeoLocation;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.wayble.server.explore.dto.search.WaybleZoneSearchConditionDto;
import com.wayble.server.explore.dto.search.WaybleZoneSearchResponseDto;
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
}