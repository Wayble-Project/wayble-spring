package com.wayble.server.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "닉네임 중복 체크 응답")
public record NicknameCheckResponse(
        @Schema(description = "닉네임 사용 가능 여부")
        boolean available,

        @Schema(description = "상세 메시지")
        String message
) {}