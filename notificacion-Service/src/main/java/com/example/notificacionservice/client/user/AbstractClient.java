package com.example.notificacionservice.client.user;

import com.example.notificacionservice.exception.RestClientNonRetryableException;
import com.example.notificacionservice.exception.RestClientRetryableException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class AbstractClient {

    private final ObjectMapper objectMapper;

    protected void handle4xxError(ClientHttpResponse response, String serviceName) {
        String errorMsg = extractErrorMessage(response, serviceName);
        log.error(errorMsg);
        throw new RestClientNonRetryableException(errorMsg);
    }

    protected void handle5xxError(ClientHttpResponse response, String serviceName) {
        String errorMsg = extractErrorMessage(response, serviceName);
        log.error(errorMsg);
        throw new RestClientRetryableException(errorMsg);
    }

    private String extractErrorMessage(ClientHttpResponse response, String serviceName) {
        try {
            ProblemDetail error = objectMapper.readValue(response.getBody(), ProblemDetail.class);
            return "Received error from %s. Detail: %s. Error properties: %s"
                    .formatted(serviceName, error.getDetail(), error.getProperties());
        } catch (IOException e) {
            return "Received unknown error while handling exception from %s. Cause: %s"
                    .formatted(serviceName, e.getMessage());
        }
    }


}
