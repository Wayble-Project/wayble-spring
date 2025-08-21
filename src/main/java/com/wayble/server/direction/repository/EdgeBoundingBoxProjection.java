package com.wayble.server.direction.repository;

import com.wayble.server.direction.entity.type.DirectionType;

public interface EdgeBoundingBoxProjection {
    Long getEdgeId();
    Long getStartNodeId();
    Long getEndNodeId();
    DirectionType getEdgeType();
    String getStartStationName();
    Double getStartLatitude();
    Double getStartLongitude();
    String getEndStationName();
    Double getEndLatitude();
    Double getEndLongitude();
    Long getRouteId();
    String getRouteName();
}
