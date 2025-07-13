package com.wayble.server.search.exception;

import com.wayble.server.common.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SearchErrorCase implements ErrorCase {

    SEARCH_EXCEPTION(400, 5001, "검색 과정에서 오류가 발생했습니다."),
    NO_SUCH_DOCUMENT(400, 5002, "해당하는 문서가 없습니다.");

    private final Integer httpStatusCode;
    private final Integer errorCode;
    private final String message;
}
