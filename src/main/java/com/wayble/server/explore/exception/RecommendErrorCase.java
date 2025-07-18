package com.wayble.server.explore.exception;

import com.wayble.server.common.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RecommendErrorCase implements ErrorCase {

    INVALID_USER(400, 6001, "잘못된 유저 정보입니다."),

    RECOMMEND_LOG_NOT_EXIST(400, 6002, "해당하는 추천 기록이 존재하지 않습니다."),

    WAYBLE_ZONE_NOT_EXIST(400, 6003, "추천 기록에 해당하는 웨이블존이 존재하지 않습니다.");

    private final Integer httpStatusCode;
    private final Integer errorCode;
    private final String message;
}
