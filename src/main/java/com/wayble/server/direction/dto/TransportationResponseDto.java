package com.wayble.server.direction.dto;

import com.wayble.server.direction.entity.DirectionType;
import io.micrometer.common.lang.Nullable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.ZonedDateTime;
import java.util.List;

@Schema(description = "대중교통 길찾기 응답 DTO")
public record TransportationResponseDto(
        List<Step> routes,
        PageInfo pageInfo
) {
    public record Step(
            DirectionType mode, // 예: START, WALK, SUBWAY, BUS, FINISH
            String routeName,// mode에 따라 null일 수 있음
            NodeInfo information, // mode에 따라 null일 수 있음
            String from,
            String to
    ) {}

    public record PageInfo(
            Integer nextCursor,
            boolean hasNext
    ) {}

    public record NodeInfo(
            List<LocationInfo> wheelchair,
            List<LocationInfo> elevator,
            Boolean accessibleRestroom
    ) {}

    public record LocationInfo(
            Double latitude,
            Double Longtitude
    ) {}
}
