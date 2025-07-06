package com.wayble.server.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wayble.server.common.exception.ErrorCase;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> {

    private Integer errorCode;
    private String message;
    private T data;

    public static <T> CommonResponse<T> success(T data) {
        return CommonResponse.<T>builder()
                .data(data)
                .build();
    }

    public static CommonResponse<Object> success() {
        return CommonResponse.builder()
                .message("success")
                .build();
    }

    public static <T> CommonResponse<T> error(ErrorCase errorCase) {
        return CommonResponse.<T>builder()
                .errorCode(errorCase.getErrorCode())
                .message(errorCase.getMessage())
                .build();
    }

    public static <T> CommonResponse<T> error(Integer errorCode, String message) {
        return CommonResponse.<T>builder()
                .errorCode(errorCode)
                .message(message)
                .build();
    }
}