package com.wayble.server.direction.dto.request;

import com.wayble.server.common.entity.Address;
import lombok.Builder;

@Builder
public record PlaceSaveRequest(
        String name,
        Address address
) {
}
