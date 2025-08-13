package com.wayble.server.direction.external.opendata.dto;

public record Arrival (    
    Integer busType1, // 1이면 저상
    Integer busType2, // 1이면 저상
    Integer term     // 배차 간격
) {}
