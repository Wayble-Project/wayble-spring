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
            @Nullable List<MoveInfo> moveInfo, // 같은 Step으로 이동한 정류장(Node) 정보 (중간 정류장만)
            @Nullable String routeName,
            Integer moveNumber, // 같은 Step(route)로 이동한 횟수
            @Nullable BusInfo busInfo, // 버스일 경우에만 생성, 이외의 경우 null
            @Nullable SubwayInfo subwayInfo, // 지하철일 경우에만 생성, 이외의 경우 null
            String from,
            String to
    ) {}

    public record PageInfo(
            Integer nextCursor,
            boolean hasNext
    ) {}

    public record MoveInfo(
            String nodeName // 정류장(Node)의 stationName
    ){}

    public record BusInfo(
            boolean isLowFloor // routeName에 "마포" 포함시 true, 그 외 버스는 false
    ){}

    public record SubwayInfo(
            List<LocationInfo> wheelchair,
            List<LocationInfo> elevator,
            Boolean accessibleRestroom
    ) {}

    public record LocationInfo(
            Double latitude,
            Double Longitude
    ) {}

    // 지하철 시설 정보 묶음 (서비스 내부에서 사용)
    public record NodeInfo(
            List<LocationInfo> wheelchair,
            List<LocationInfo> elevator,
            Boolean accessibleRestroom
    ) {}
}
