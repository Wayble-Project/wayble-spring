package com.wayble.server.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserPlaceCreateRequestDto(
        @NotBlank(message = "제목은 필수입니다.") String title,
        String color
) {}
