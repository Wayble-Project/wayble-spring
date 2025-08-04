package com.wayble.server.common.dto;

import lombok.Builder;

@Builder
public record FacilityDto(
        boolean hasSlope,
        boolean hasNoDoorStep,
        boolean hasElevator,
        boolean hasTableSeat,
        boolean hasDisabledToilet,
        String floorInfo
) {}