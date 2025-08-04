package com.wayble.server.wayblezone.dto;

import com.wayble.server.common.dto.FacilityDto;
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
        long reviewCount,
        String contactNumber,
        String imageUrl,
        FacilityDto facilities,
        Map<String, BusinessHourDto> businessHours,
        List<String> photos
) {
    @Builder
    public record BusinessHourDto(
            String open,
            String close
    ) {}
}