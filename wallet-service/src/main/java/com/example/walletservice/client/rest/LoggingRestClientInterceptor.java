package com.example.walletservice.client.rest;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.walletservice.utils.MaskingUtil.maskIfNeeded;

@Slf4j
public class LoggingRestClientInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(@NonNull HttpRequest request, @NonNull byte[] body, ClientHttpRequestExecution execution) throws IOException {
        log.info("Client request. Method '{}'. URI '{}'. Headers: '{}'.",
                extractMethod(request), extractUri(request), extractHeaders(request.getHeaders()));
        ClientHttpResponse response = execution.execute(request, body);
        log.info("Client response. Status '{}'. Method '{}'. URI '{}'. Headers '{}'.",
                extractStatus(response),
                extractMethod(request),
                extractUri(request),
                extractHeaders(response.getHeaders()));
        return response;
    }

    private static String extractMethod(HttpRequest request) {
        return request.getMethod().toString();
    }

    private static String extractUri(HttpRequest request) {
        return request.getURI().toString();
    }

    private static String extractHeaders(HttpHeaders headers) {
        return headers.headerSet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, LoggingRestClientInterceptor::getHeaderValue))
                .entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(", "));
    }

    private static int extractStatus(ClientHttpResponse response) {
        try {
            return response.getStatusCode().value();
        } catch (IOException e) {
            return 0;
        }
    }

    private static String getHeaderValue(Map.Entry<String, List<String>> entry) {
        return entry.getValue().stream()
                .map(value -> maskIfNeeded(entry.getKey(), value))
                .collect(Collectors.joining(", "));
    }

}
