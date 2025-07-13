package com.wayble.server.wayblezone.dto;

import lombok.Builder;

@Builder
public record WaybleZoneListResponseDto(
        Long waybleZoneId,
        String name,
        String category,
        String address,
        double rating,
        int reviewCount,
        String imageUrl,
        String contactNumber,
        FacilityDto facilities
) {
    @Builder
    public record FacilityDto(
            boolean hasSlope,
            boolean hasNoDoorStep,
            boolean hasElevator,
            boolean hasTableSeat,
            boolean hasDisabledToilet,
            String floorInfo
    ) {}
}

