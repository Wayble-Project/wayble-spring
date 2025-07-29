package com.wayble.server.direction.exception;

import com.wayble.server.common.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WalkingErrorCase implements ErrorCase {

    T_MAP_API_FAILED(500, 8001, "T MAP API 호출에 실패했습니다."),
    NODE_NOT_FOUND(400, 8002, "해당 위도, 경도 근처의 노드가 존재하지 않습니다."),
    ;

    private final Integer httpStatusCode;
    private final Integer errorCode;
    private final String message;
}
