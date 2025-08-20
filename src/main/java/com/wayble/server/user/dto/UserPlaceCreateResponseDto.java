package com.wayble.server.user.dto;

import lombok.Builder;

@Builder
public record UserPlaceCreateResponseDto(
        Long placeId,
        String title,
        String color,
        String message
) {}
