package com.wayble.server.auth.exception;

import com.wayble.server.common.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCase implements ErrorCase {
    UNAUTHORIZED(401, 7001, "인증 정보가 없거나 userId를 추출할 수 없습니다.");

    private final Integer httpStatusCode;
    private final Integer errorCode;
    private final String message;
}
