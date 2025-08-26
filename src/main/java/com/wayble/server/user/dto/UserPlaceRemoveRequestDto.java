package com.wayble.server.user.dto;

import jakarta.validation.constraints.NotNull;

public record UserPlaceRemoveRequestDto(
        @NotNull Long placeId,
        @NotNull Long waybleZoneId
) {}
