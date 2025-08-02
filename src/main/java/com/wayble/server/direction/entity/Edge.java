package com.wayble.server.direction.entity;

import java.util.List;

public record Edge(
        long from,
        long to,
        double length,
        List<double[]> geometry
) {
}
