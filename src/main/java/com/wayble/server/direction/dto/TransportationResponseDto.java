package com.wayble.server.direction.dto;

import com.wayble.server.direction.entity.DirectionType;
import org.springframework.lang.Nullable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "대중교통 길찾기 응답 DTO")
public record TransportationResponseDto(
        List<Step> routes,
        PageInfo pageInfo
) {
    public record Step(
            DirectionType mode, // 예: START, WALK, SUBWAY, BUS, FINISH
            @Nullable String routeName,
            @Nullable NodeInfo information,
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
            Double Longitude
    ) {}
}
