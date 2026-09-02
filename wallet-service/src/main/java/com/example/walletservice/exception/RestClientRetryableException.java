package com.example.walletservice.exception;

public class RestClientRetryableException extends RuntimeException{
    public RestClientRetryableException(String message) {
        super(message);
    }
}
