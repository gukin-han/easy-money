package com.easymoney.global.error;

import lombok.Getter;

@Getter
public class KisApiException extends RuntimeException {

    private final String returnCode;

    public KisApiException(String returnCode, String message) {
        super(message);
        this.returnCode = returnCode;
    }
}
