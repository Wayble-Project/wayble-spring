package com.wayble.server.user.dto;

import lombok.Builder;

@Builder
public record KakaoLoginResponseDto(
        String accessToken,
        String refreshToken,
        boolean isNewUser,
        UserDto user
) {
    @Builder
    public record UserDto(
            Long id,
            String nickname,
            String email
    ) {}
}