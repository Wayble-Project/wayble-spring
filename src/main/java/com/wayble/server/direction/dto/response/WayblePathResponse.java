package com.wayble.server.direction.dto.response;

import com.wayble.server.direction.entity.type.Type;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "웨이블 추천 경로 API")
public record WayblePathResponse(

        @Schema(description = "총 거리", example = "1365")
        int distance,

        @Schema(description = "총 소요 시간", example = "909")
        int time,

        @Schema(description = "위치", example = "[{\"lat\":37.4941736,\"lon\":127.0247425,\"type\":\"RAMP\"}]")
        List<WayblePoint> points,

        @Schema(description = "좌표 리스트", example = "[[127.0247425,37.4941736],[127.0249966,37.4942539]]")
        List<double[]> polyline
) {
    public record WayblePoint(
            @Schema(description = "위도", example = "37.4941736")
            double lat,

            @Schema(description = "경도", example = "127.0247425")
            double lon,

            @Schema(description = "웨이블 마커 타입", example = "RAMP")
            Type type
    ) {}

    public static WayblePathResponse of(
            int distance,
            int time,
            List<WayblePoint> points,
            List<double[]> polyline
    ) {
        List<WayblePoint> wayblePoints = points.stream()
                .map(n -> new WayblePoint(n.lat, n.lon, n.type))
                .toList();

        return WayblePathResponse.builder()
                .distance(distance)
                .time(time)
                .points(wayblePoints)
                .polyline(polyline)
                .build();
    }
}
