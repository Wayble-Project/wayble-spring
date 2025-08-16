package com.wayble.server.user.dto;

import com.wayble.server.wayblezone.dto.WaybleZoneListResponseDto;
import lombok.Builder;

import java.util.List;

@Builder
public record UserPlaceZonesResponseDto(
        Long placeId,
        String title,
        String color,
        int savedCount,
        List<WaybleZoneListResponseDto> zones
) {}