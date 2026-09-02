package com.example.walletservice.client.usd;

import com.example.walletservice.client.UsdClient;
import com.example.walletservice.client.usd.response.ResponseUsd;
import com.example.walletservice.properties.UsdClientProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;

import static com.example.walletservice.utils.MaskingUtil.maskIfNeeded;

@Slf4j
@Component
public class UsdClientImpl extends AbstractClient implements UsdClient {

    private static final String EXCHANGE_BACKEND = "usdBackend";
    private static final String SERVICE_NAME = "exchange-service";
    private final UsdClientProperties properties;
    private final RestClient usdRestClient;

    public UsdClientImpl(@Autowired(required = false)ObjectMapper objectMapper, UsdClientProperties properties, RestClient usdRestClient) {
        super(objectMapper);
        this.properties = properties;
        this.usdRestClient = usdRestClient;
    }

    @CircuitBreaker(name = EXCHANGE_BACKEND)
    @Retry(name = EXCHANGE_BACKEND)
    @Cacheable(value = "usdCourseCache")
    @Override
    public ResponseUsd getCourse() {
        ResponseUsd body = usdRestClient.get()
                .uri(properties.getNearestPath())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(ResponseUsd.class);

        Assert.notNull(body, "%s returned null body".formatted(SERVICE_NAME));
        log.info("{} returned {}", SERVICE_NAME, maskIfNeeded("body", body));
        return body;
    }
}

