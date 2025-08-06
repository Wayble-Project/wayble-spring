package com.wayble.server.direction.dto.request;

import com.wayble.server.common.entity.Address;
import lombok.Builder;

import java.util.List;

@Builder
public record PlaceSaveRequest(
        List<PlaceDetailRequest> requests
) {

    @Builder
    public record PlaceDetailRequest(
            String name,
            Address address
    ) {}
}
