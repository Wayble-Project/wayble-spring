package com.wayble.server.direction.exception;

import com.wayble.server.common.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WalkingErrorCase implements ErrorCase {

    T_MAP_API_FAILED(500, 8001, "T MAP API 호출에 실패했습니다."),
    ;

    private final Integer httpStatusCode;
    private final Integer errorCode;
    private final String message;
}
