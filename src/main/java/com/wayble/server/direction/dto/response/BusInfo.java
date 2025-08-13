package com.wayble.server.direction.dto.response;

import java.util.List;

public record BusInfo(List<BusArrival> buses, String stationName) {
    public record BusArrival(
        String busNumber, 
        String arrival1, 
        String arrival2
    ) {}
}
