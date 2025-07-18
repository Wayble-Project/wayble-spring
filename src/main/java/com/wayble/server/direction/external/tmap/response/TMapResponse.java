package com.wayble.server.direction.external.tmap.response;

import java.util.List;

public record TMapResponse(
        String type,
        List<Feature> features
) {

    public record Feature(
            String type,
            Geometry geometry,
            Properties properties
    ) {}

    public record Geometry(
            String type,
            List<Object> coordinates
    ) {}

    public record Properties(
            Integer totalDistance,
            Integer totalTime,
            Integer index,
            Integer pointIndex,
            String name,
            String description,
            String direction,
            String nearPoiName,
            String nearPoiX,
            String nearPoiY,
            String intersectionName,
            String facilityType,
            String facilityName,
            Integer turnType,
            String pointType,
            Integer distance,
            Integer time,
            Integer roadType,
            Integer categoryRoadType,
            Integer lineIndex
    ) {}
}
