package com.wayble.server.wayblezone.exception;

import com.wayble.server.common.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WaybleZoneErrorCase implements ErrorCase {

    WAYBLE_ZONE_NOT_FOUND(404, 2001, "해당 웨이블존을 찾을 수 없습니다."),
    INVALID_CATEGORY(400, 2002, "유효하지 않은 category 파라미터입니다.");

    private final Integer httpStatusCode;
    private final Integer errorCode;
    private final String message;
}
