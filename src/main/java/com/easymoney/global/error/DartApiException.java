package com.easymoney.global.error;

import lombok.Getter;

@Getter
public class DartApiException extends RuntimeException {

    private final String status;

    public DartApiException(String status, String message) {
        super(message);
        this.status = status;
    }
}
