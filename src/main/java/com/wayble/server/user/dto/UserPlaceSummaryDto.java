package com.wayble.server.user.dto;

import lombok.Builder;

@Builder
public record UserPlaceSummaryDto(
        Long placeId,
        String title,
        String color,
        int savedCount
) {}