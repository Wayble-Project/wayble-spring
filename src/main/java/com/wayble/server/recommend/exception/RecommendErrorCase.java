package com.wayble.server.recommend.exception;

import com.wayble.server.common.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RecommendErrorCase implements ErrorCase {

    INVALID_USER(400, 6001, "잘못된 유저 정보입니다.");

    private final Integer httpStatusCode;
    private final Integer errorCode;
    private final String message;
}
