package com.example.notificacionservice.exception;

public class RestClientRetryableException extends RuntimeException{
    public RestClientRetryableException(String message) {
        super(message);
    }
}
