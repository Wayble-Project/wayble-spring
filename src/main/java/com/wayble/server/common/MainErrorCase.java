package com.wayble.server.common;

import com.wayble.server.common.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MainErrorCase implements ErrorCase {

    MAIN_TEST_ERROR(400, 1001, "예외 메시지");

    private final Integer httpStatusCode;
    private final Integer errorCode;
    private final String message;
}
