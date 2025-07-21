package com.wayble.server.explore.dto;

import com.wayble.server.explore.entity.EsWaybleZoneFacility;
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
    public static FacilityResponseDto from(EsWaybleZoneFacility facility) {
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