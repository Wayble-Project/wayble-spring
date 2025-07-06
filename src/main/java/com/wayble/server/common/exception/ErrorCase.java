package com.wayble.server.common.exception;

public interface ErrorCase {

    Integer getHttpStatusCode();

    Integer getErrorCode();

    String getMessage();
}
