package com.wayble.server.wayblezone.dto;

import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record WaybleZoneDetailResponseDto(
        Long waybleZoneId,
        String name,
        String category,
        String address,
        double rating,
        int reviewCount,
        String contactNumber,
        String imageUrl,
        FacilityDto facilities,
        Map<String, BusinessHourDto> businessHours,
        List<String> photos
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

    @Builder
    public record BusinessHourDto(
            String open,
            String close
    ) {}
}