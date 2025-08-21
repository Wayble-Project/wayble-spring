package com.wayble.server.direction.dto;

public record NodeRef(
    Long id, 
    String stationName, 
    Double latitude, 
    Double longitude
) {}
