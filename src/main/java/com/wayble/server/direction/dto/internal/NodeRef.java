package com.wayble.server.direction.dto.internal;

public record NodeRef(
    Long id, 
    String stationName, 
    Double latitude, 
    Double longitude
) {}
