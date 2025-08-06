package com.wayble.server.direction.service;

import com.wayble.server.direction.dto.request.DirectionSearchRequest;
import com.wayble.server.direction.dto.request.PlaceSaveRequest;
import com.wayble.server.direction.dto.response.DirectionSearchResponse;
import com.wayble.server.direction.entity.DirectionDocument;
import com.wayble.server.direction.entity.Place;
import com.wayble.server.direction.repository.DirectionElasticsearchRepository;
import com.wayble.server.direction.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectionService {

    private final PlaceRepository placeRepository;
    private final DirectionElasticsearchRepository directionElasticsearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Transactional
    public void savePlaceAndIndexDocument(List<PlaceSaveRequest.PlaceDetailRequest> requests) {
        List<Place> places = requests.stream()
                .map(request ->
                    Place.of(request.name(), request.address())
                )
                .toList();

        placeRepository.saveAll(places);

        List<IndexQuery> queries = places.stream()
                .map(place ->
                        new IndexQueryBuilder()
                                .withId(place.getId().toString())
                                .withObject(DirectionDocument.from(place))
                                .build()
                )
                .toList();

        elasticsearchOperations.bulkIndex(queries, IndexCoordinates.of("direction"));
    }

    public List<DirectionSearchResponse> searchDirection(String keyword) {
        DirectionSearchRequest request = DirectionSearchRequest.builder()
                .name(keyword)
                .build();

        return directionElasticsearchRepository.searchDirection(request);
    }
}
