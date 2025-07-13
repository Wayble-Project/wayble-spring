package com.wayble.server.wayblezone.dto;

import com.wayble.server.common.dto.FacilityDto;
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
) {}

