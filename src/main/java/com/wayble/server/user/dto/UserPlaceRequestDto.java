package com.wayble.server.user.dto;

import jakarta.validation.constraints.NotNull;

public record UserPlaceRequestDto(
        @NotNull Long waybleZoneId,
        @NotNull String title,
        String color
) {}
