package com.wayble.server.direction.exception;

import com.wayble.server.common.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DirectionErrorCase implements ErrorCase {

    REVIEW_NOT_FOUND(400, 4001, "길찾기 정보를 찾을 수 없습니다.");

    private final Integer httpStatusCode;
    private final Integer errorCode;
    private final String message;
}

