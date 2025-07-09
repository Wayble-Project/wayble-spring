package com.wayble.server.direction.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "길찾기 요청 DTO")
public record TransportationRequestDto (
        Location origin,
        Location destination,
        String cursor,
        Integer size

) {     public record Location(
        String name,
        double latitude,
        double longitude
) {}
}
