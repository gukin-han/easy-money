package com.easymoney.global.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DartApiException.class)
    public ResponseEntity<ErrorResponse> handleDartApiException(DartApiException e) {
        ErrorResponse response = new ErrorResponse(
                "DART_API_ERROR",
                e.getMessage() + " (status: " + e.getStatus() + ")"
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorResponse> handleRestClientException(RestClientException e) {
        ErrorResponse response = new ErrorResponse(
                "EXTERNAL_API_ERROR",
                "외부 API 통신 실패: " + e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }
}
