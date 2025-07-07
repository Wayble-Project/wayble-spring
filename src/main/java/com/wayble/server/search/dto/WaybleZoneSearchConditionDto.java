package com.wayble.server.search.dto;

import com.wayble.server.wayblezone.entity.WaybleZoneType;
import lombok.Builder;
import lombok.NonNull;

@Builder
public record WaybleZoneSearchConditionDto(
        @NonNull
        Double latitude,
        @NonNull
        Double longitude,
        Double radiusKm,
        String name,
        WaybleZoneType zoneType
) {
}
