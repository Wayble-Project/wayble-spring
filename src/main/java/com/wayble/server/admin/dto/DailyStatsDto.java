package com.wayble.server.admin.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record DailyStatsDto(
        LocalDate date,
        long dailyRegistrationCount,
        long dailyActiveUserCount,
        long totalUserCount,
        long totalWaybleZoneCount
) {
}