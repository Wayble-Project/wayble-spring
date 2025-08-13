package com.wayble.server.explore.dto.facility;

import com.wayble.server.explore.entity.FacilityType;
import com.wayble.server.explore.entity.WaybleFacilityDocument;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record WaybleFacilityResponseDto(
        Double latitude,

        Double longitude,

        FacilityType facilityType
) {
    public static WaybleFacilityResponseDto from(WaybleFacilityDocument facilityDocument) {
        return WaybleFacilityResponseDto.builder()
                .latitude(facilityDocument.getLocation().getLat())
                .longitude(facilityDocument.getLocation().getLon())
                .facilityType(facilityDocument.getFacilityType())
                .build();
    }
}
