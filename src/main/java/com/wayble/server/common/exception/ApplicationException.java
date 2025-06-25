package com.wayble.server.common.exception;

import lombok.Getter;

@Getter
public class ApplicationException extends RuntimeException {

    private final ErrorCase errorCase;

    public ApplicationException(ErrorCase errorCase) {
        super(errorCase.getMessage());
        this.errorCase = errorCase;
    }
}
