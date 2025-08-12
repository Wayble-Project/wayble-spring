package com.wayble.server.user.dto;

import lombok.Builder;



@Builder
public record UserPlaceListResponseDto(
        Long placeId,
        String title,
        WaybleZoneDto waybleZone
) {
    @Builder
    public record WaybleZoneDto(
            Long waybleZoneId,
            String name,
            String category,
            double rating,
            String address,
            String imageUrl
    ) {}
}