package com.wayble.server.common.swagger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "에러 응답 포맷")
public class ErrorResponseDoc {

    @Schema(description = "에러 코드", example = "3001")
    private Integer errorCode;

    @Schema(description = "에러 메시지", example = "리뷰를 찾을 수 없습니다.")
    private String message;
}