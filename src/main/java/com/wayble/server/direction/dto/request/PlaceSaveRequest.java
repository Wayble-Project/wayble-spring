package com.wayble.server.direction.dto.request;

import lombok.Builder;

@Builder
public record PlaceSaveRequest(
        String name,
        String address,
        long latitude,
        long longitude
) {
}
