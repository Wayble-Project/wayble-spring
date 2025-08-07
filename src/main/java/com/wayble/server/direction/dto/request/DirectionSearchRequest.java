package com.wayble.server.direction.dto.request;

import lombok.Builder;

@Builder
public record DirectionSearchRequest(
        String name,
        Double latitude,
        Double longitude
) {
}
