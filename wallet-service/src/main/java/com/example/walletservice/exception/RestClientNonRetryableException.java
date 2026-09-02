package com.example.walletservice.exception;

public class RestClientNonRetryableException extends RuntimeException {
    public RestClientNonRetryableException(String message) {
        super(message);
    }
}
