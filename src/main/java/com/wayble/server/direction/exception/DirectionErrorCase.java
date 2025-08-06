package com.wayble.server.direction.exception;

import com.wayble.server.common.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DirectionErrorCase implements ErrorCase {

    PATH_NOT_FOUND(400, 4001, "해당하는 경로를 찾을 수 없습니다."),
    ES_INDEXING_FAILED(500, 4002, "ElasticSearch 인덱싱에 실패했습니다."),
    HISTORY_NOT_FOUND(400, 4004, "검색 기록이 없습니다."),
    ;

    private final Integer httpStatusCode;
    private final Integer errorCode;
    private final String message;
}

