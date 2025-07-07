package com.wayble.server.search.service;

import com.wayble.server.common.entity.Address;
import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.search.entity.WaybleZoneDocument;
import com.wayble.server.search.exception.SearchErrorCase;
import com.wayble.server.search.repository.WaybleZoneSearchRepository;
import com.wayble.server.wayblezone.entity.WaybleZone;
import com.wayble.server.wayblezone.entity.WaybleZoneType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    private final WaybleZoneSearchRepository waybleZoneSearchRepository;

    public void makeException() {
        throw new ApplicationException(SearchErrorCase.SEARCH_EXCEPTION);
    }

    public WaybleZoneDocument getWaybleZoneDocumentById(Long id) {
        return waybleZoneSearchRepository.findById(id).orElse(null);
    }

    public void save() {
        Address address = Address.builder()
                .state("state")
                .city("city")
                .streetAddress("streetAddress")
                .detailAddress("detailAddress")
                .district("district")
                .latitude(37.123456789)
                .longitude(37.123456789)
                .build();

        WaybleZoneDocument waybleZoneDocument = new WaybleZoneDocument(
                1L,
                "testZone",
                WaybleZoneType.CAFE,
                "thumbnailImage",
                address,
                0.0,
                0L
        );
        elasticsearchOperations.save(waybleZoneDocument);
    }
}
