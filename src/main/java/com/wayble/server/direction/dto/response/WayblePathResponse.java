package com.wayble.server.direction.dto.response;

import com.wayble.server.direction.entity.type.Type;
import lombok.Builder;

import java.util.List;

@Builder
public record WayblePathResponse(
        double time,
        double distance,
        List<WayblePoint> points,
        List<double[]> polyline
) {
    public record WayblePoint(
            long id,
            double lat,
            double lon,
            Type type
    ) {}

    public static WayblePathResponse of(
            double time,
            double distance,
            List<WayblePoint> points,
            List<double[]> polyline
    ) {
        List<WayblePoint> wayblePoints = points.stream()
                .map(n -> new WayblePoint(n.id, n.lat, n.lon, n.type))
                .toList();

        return WayblePathResponse.builder()
                .time(time)
                .distance(distance)
                .points(wayblePoints)
                .polyline(polyline)
                .build();
    }
}
