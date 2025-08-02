package com.wayble.server.direction.entity;

import com.wayble.server.direction.entity.type.Type;

public record WaybleMarker(
        Long id,
        double lat,
        double lon,
        Type type
) {
}
