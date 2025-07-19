package com.wayble.server.user.dto;

import lombok.Builder;



@Builder
public record UserPlaceListResponseDto(
        Long place_id,
        String title,
        WaybleZoneDto wayble_zone
) {
    @Builder
    public record WaybleZoneDto(
            Long wayble_zone_id,
            String name,
            String category,
            double rating,
            String address,
            String image_url
    ) {}
}