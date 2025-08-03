package com.wayble.server.direction.service;

import com.wayble.server.direction.dto.request.PlaceSaveRequest;
import com.wayble.server.direction.entity.Place;
import com.wayble.server.direction.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DirectionService {

    private final PlaceRepository placeRepository;

    public void savePlace(PlaceSaveRequest request) {
        Place place = Place.of(
                request.name(),
                request.address(),
                request.latitude(),
                request.longitude()
        );
        placeRepository.save(place);
    }
}
