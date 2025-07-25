package com.wayble.server.user.dto;

import jakarta.validation.constraints.NotBlank;

public record KakaoLoginRequestDto(
        @NotBlank(message = "액세스 토큰은 필수입니다")
        String accessToken
) {}