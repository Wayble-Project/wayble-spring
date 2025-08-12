package com.wayble.server.explore.dto.common;

import com.wayble.server.explore.entity.EsWaybleFacility;
import lombok.Builder;

@Builder
public record FacilityResponseDto(
        Boolean hasSlope,
        Boolean hasNoDoorStep,
        Boolean hasElevator,
        Boolean hasTableSeat,
        Boolean hasDisabledToilet,
        String floorInfo
) {
    public static FacilityResponseDto from(EsWaybleFacility facility) {
        if (facility == null) {
            return null;
        }
        
        return FacilityResponseDto.builder()
                .hasSlope(facility.isHasSlope())
                .hasNoDoorStep(facility.isHasNoDoorStep())
                .hasElevator(facility.isHasElevator())
                .hasTableSeat(facility.isHasTableSeat())
                .hasDisabledToilet(facility.isHasDisabledToilet())
                .floorInfo(facility.getFloorInfo())
                .build();
    }
}