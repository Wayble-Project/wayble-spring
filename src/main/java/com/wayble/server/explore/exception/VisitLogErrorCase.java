package com.wayble.server.explore.exception;

import com.wayble.server.common.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VisitLogErrorCase implements ErrorCase {

    USER_NOT_EXIST(400, 7001, "해당하는 유저가 존재하지 않습니다."),
    ZONE_NOT_EXIST(400, 7002, "해당하는 웨이블존이 존재하지 않습니다.");

    private final Integer httpStatusCode;
    private final Integer errorCode;
    private final String message;
}
