package com.wayble.server.explore.dto.facility;

import com.wayble.server.explore.entity.FacilityType;

public record WaybleFacilityResponseDto(
        Double latitude,

        Double longitude,

        FacilityType facilityType
) {
}
