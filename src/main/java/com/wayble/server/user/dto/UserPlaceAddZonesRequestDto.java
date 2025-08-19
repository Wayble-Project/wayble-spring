package com.wayble.server.user.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UserPlaceAddZonesRequestDto(
        @NotNull Long placeId,
        @NotEmpty List<Long> waybleZoneId
) {}
