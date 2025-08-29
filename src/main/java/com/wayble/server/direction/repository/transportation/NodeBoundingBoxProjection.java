package com.wayble.server.direction.repository.transportation;

import com.wayble.server.direction.entity.type.DirectionType;

public interface NodeBoundingBoxProjection {
    Long getId();
    String getStationName();
    DirectionType getNodeType();
    Double getLatitude();
    Double getLongitude();
}
