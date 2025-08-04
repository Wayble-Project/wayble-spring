package com.wayble.server.direction.dto.response;

import com.wayble.server.direction.entity.DirectionDocument;
import lombok.Builder;

@Builder
public record DirectionSearchResponse(
        Long placeId,
        String name,
        String address,
        Double latitude,
        Double longitude
) {
    public static DirectionSearchResponse from(DirectionDocument directionDocument) {
        return DirectionSearchResponse.builder()
                .placeId(directionDocument.getId())
                .name(directionDocument.getName())
                .address(directionDocument.getEsAddress().getStreetAddress())
                .latitude(directionDocument.getEsAddress().getLocation().getLat())
                .longitude(directionDocument.getEsAddress().getLocation().getLon())
                .build();
    }
}
