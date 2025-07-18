package com.wayble.server.direction.external.tmap.request;

import lombok.Builder;

@Builder
public record TMapRequest(
        double startX,
        double startY,
        double endX,
        double endY,
        String startName,
        String endName,
        Integer searchOption,
        String sort
) {
}
