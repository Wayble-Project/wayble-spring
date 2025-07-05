package com.wayble.server.wayblezone.exception;

import com.wayble.server.common.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WaybleZoneErrorCase implements ErrorCase {

    WAYBLE_ZONE_NOT_FOUND(400, 2001, "웨이블존을 찾을 수 없습니다.");

    private final Integer httpStatusCode;
    private final Integer errorCode;
    private final String message;
}
