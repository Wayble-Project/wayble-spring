package com.wayble.server.aws;

import com.wayble.server.common.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AwsErrorCase implements ErrorCase {

    IMAGE_UPLOAD_ERROR(400, 8001, "파일 업로드 과정에서 문제가 발생했습니다.");

    private final Integer httpStatusCode;
    private final Integer errorCode;
    private final String message;
}
