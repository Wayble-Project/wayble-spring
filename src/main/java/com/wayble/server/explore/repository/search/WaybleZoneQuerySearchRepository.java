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

        // 1) Build the query using the new lambda-based builders
        Query query = Query.of(q -> q
                .bool(b -> {
                    // Must clause for zoneType if it exists
                    if (cond.zoneType() != null) {
                        b.must(m -> m
                                .term(t -> t
                                        .field("zoneType.keyword")
                                        .value(cond.zoneType().name())
                                )
                        );
                    }
                    // Must clause for name if it exists and is not blank
                    if (cond.zoneName() != null && !cond.zoneName().isBlank()) {
                        b.must(m -> m
                                .match(mp -> mp
                                        .field("zoneName")
                                        .query(cond.zoneName())
                                )
                        );
                    }
                    // Filter by geo distance
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

        // 2) Build the sort options using the new builder
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

        // 3) Combine into a NativeQuery
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(query)
                .withSort(geoSort)
                .withPageable(PageRequest.of(
                        pageable.getPageNumber(),
                        fetchSize
                ))
                .build();

        // 4) Execute the search
        SearchHits<WaybleZoneDocument> hits =
                operations.search(nativeQuery, WaybleZoneDocument.class, INDEX);

        // 5) Map to DTO: The distance in sortValues is still accessible in the same way
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

        boolean hasNext = dtos.size() > pageable.getPageSize();
        if (hasNext) {
            dtos = dtos.subList(0, pageable.getPageSize());
        }

        return new SliceImpl<>(dtos, pageable, hasNext);
    }
}