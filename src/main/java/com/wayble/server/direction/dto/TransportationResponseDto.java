package com.wayble.server.direction.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.ZonedDateTime;
import java.util.List;

@Schema(description = "대중교통 길찾기 응답 DTO")
public record TransportationResponseDto(
        List<Route> routes,
        PageInfo pageInfo
) {
    public record Route(
            int totalTime,
            ZonedDateTime arriveTime,
            List<Step> steps
    ) {}

    public record Step(
            String mode, // 예: START, WALK, SUBWAY, BUS, FINISH
            String address, // mode에 따라 null일 수 있음
            String from,
            String to,
            String line,
            List<Path> path, // WALK일 때 사용
            SubwayInformation information, // SUBWAY일 때 사용
            String steps // BUS일 때 사용
    ) {}

    public record Path(
            String mode, // WALK 등
            int time // 분 단위
    ) {}

    public record SubwayInformation(
            List<String> wheelchair,
            List<String> elevator,
            boolean accessibleRestroom
    ) {}

    public record PageInfo(
            String nextCursor,
            boolean hasNext
    ) {}
}
