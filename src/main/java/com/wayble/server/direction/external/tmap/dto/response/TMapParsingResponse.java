package com.wayble.server.direction.external.tmap.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record TMapParsingResponse(
        int totalDistance,
        int totalTime,
        List<Step> steps
) {

    public record Step(
           String type,
           String name,
           String description,
           Coordinate coordinate,
           List<Coordinate> coordinates,
           Integer turnType,
           String pointType,
           Integer distance,
           Integer time
    ) {}

    public record Coordinate(
            double longitude,
            double latitude
    ) {}
}
