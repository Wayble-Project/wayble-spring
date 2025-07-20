package com.wayble.server.direction.external.tmap.mapper;

import com.wayble.server.direction.external.tmap.dto.response.TMapParsingResponse;
import com.wayble.server.direction.external.tmap.dto.response.TMapResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TMapMapper {

    private static final String GEOMETRY_TYPE_POINT = "Point";
    private static final String GEOMETRY_TYPE_LINESTRING = "LineString";
    private static final String POINT_TYPE_START = "SP";
    private static final String STEP_TYPE_POINT = "point";
    private static final String STEP_TYPE_LINE = "line";

    public TMapParsingResponse parseResponse(TMapResponse response) {
        List<TMapParsingResponse.Step> steps = new ArrayList<>();
        int totalDistance = 0;
        int totalTime = 0;

        for (TMapResponse.Feature feature : response.features()) {
            TMapResponse.Geometry geometry = feature.geometry();
            TMapResponse.Properties properties = feature.properties();

            // 건물일 경우
            if (GEOMETRY_TYPE_POINT.equalsIgnoreCase(geometry.type())) {
                List<?> coordinates = geometry.coordinates();

                if (coordinates.size() >= 2) {
                    double longitude = ((Number) coordinates.get(0)).doubleValue();
                    double latitude = ((Number) coordinates.get(1)).doubleValue();

                    // 출발지일 경우
                    if (POINT_TYPE_START.equals(properties.pointType())) {
                        totalDistance = properties.totalDistance() != null ? properties.totalDistance() : 0;
                        totalTime = properties.totalTime() != null ? properties.totalTime() : 0;
                    }

                    TMapParsingResponse.Step step = new TMapParsingResponse.Step(
                            STEP_TYPE_POINT,
                            properties.name(),
                            properties.description(),
                            new TMapParsingResponse.Coordinate(longitude, latitude),
                            null,
                            properties.turnType(),
                            properties.pointType(),
                            null,
                            null
                    );
                    steps.add(step);
                }
            // 도로일 경우
            } else if (GEOMETRY_TYPE_LINESTRING.equalsIgnoreCase(geometry.type())) {
                List<TMapParsingResponse.Coordinate> coordinates = new ArrayList<>();

                for (Object object : geometry.coordinates()) {
                    if (object instanceof List<?> pointList && pointList.size() >= 2) {
                        double longitude = ((Number) pointList.get(0)).doubleValue();
                        double latitude = ((Number) pointList.get(1)).doubleValue();
                        coordinates.add(new TMapParsingResponse.Coordinate(longitude, latitude));
                    }
                }

                TMapParsingResponse.Step step = new TMapParsingResponse.Step(
                        STEP_TYPE_LINE,
                        properties.name(),
                        properties.description(),
                        null,
                        coordinates,
                        null,
                        null,
                        properties.distance(),
                        properties.time()
                );
                steps.add(step);
            }
        }
        return new TMapParsingResponse(totalDistance, totalTime, steps);
    }
}
