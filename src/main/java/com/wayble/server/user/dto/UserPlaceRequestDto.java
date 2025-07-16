package com.wayble.server.user.dto;

import jakarta.validation.constraints.NotNull;

public record UserPlaceRequestDto(
        @NotNull Long userId,
        @NotNull Long waybleZoneId,
        @NotNull String title
) {}
