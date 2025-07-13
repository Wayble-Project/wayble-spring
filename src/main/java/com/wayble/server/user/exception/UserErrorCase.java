package com.wayble.server.user.exception;

import com.wayble.server.common.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCase implements ErrorCase {

    USER_NOT_FOUND(400, 1001, "사용자를 찾을 수 없습니다."),
    WAYBLE_ZONE_NOT_FOUND(404, 1002, "해당 웨이블존을 찾을 수 없습니다."),
    PLACE_ALREADY_SAVED(400, 1003, "이미 저장한 장소입니다.");

    private final Integer httpStatusCode;
    private final Integer errorCode;
    private final String message;
}
