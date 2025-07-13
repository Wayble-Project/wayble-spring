package com.wayble.server.user.dto;

import jakarta.validation.constraints.NotNull;

public record UserPlaceSaveRequestDto(
        @NotNull Long userId,
        @NotNull Long waybleZoneId,
        @NotNull String title
) {}
