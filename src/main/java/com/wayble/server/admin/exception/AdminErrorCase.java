package com.wayble.server.admin.exception;

import com.wayble.server.common.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AdminErrorCase implements ErrorCase {

    USER_NOT_FOUND(404, 9001, "사용자를 찾을 수 없습니다."),
    WAYBLE_ZONE_NOT_FOUND(404, 9002, "해당 웨이블존을 찾을 수 없습니다.");

    private final Integer httpStatusCode;
    private final Integer errorCode;
    private final String message;
}
