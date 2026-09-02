package com.example.notificacionservice.exception;

public class RestClientNonRetryableException extends RuntimeException {
    public RestClientNonRetryableException(String message) {
        super(message);
    }
}
