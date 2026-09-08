package com.example.walletservice.client.banking;

import com.example.walletservice.client.banking.payload.request.PaymentData;
import com.example.walletservice.client.banking.payload.response.DataResponse;
import com.example.walletservice.client.usd.AbstractClient;
import com.example.walletservice.exception.RestClientNonRetryableException;
import com.example.walletservice.exception.RestClientRetryableException;
import com.example.walletservice.properties.UsdClientProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;

import static com.example.walletservice.utils.MaskingUtil.maskIfNeeded;

@Slf4j
@Component
public class BankingClientImpl extends AbstractClient implements BankingClient {

    private static final String BANKING_BACKEND = "bankingBackend";
    private static final String SERVICE_NAME = "banking-service";
    private final UsdClientProperties properties;
    private final RestClient bankingRestClient;

    public BankingClientImpl(@Autowired(required = false) ObjectMapper objectMapper, UsdClientProperties properties, RestClient bankingRestClient) {
        super(objectMapper);
        this.properties = properties;
        this.bankingRestClient = bankingRestClient;
    }

    @CircuitBreaker(name = BANKING_BACKEND)
    @Retry(name = BANKING_BACKEND)
    @Override
    public DataResponse getQrCode(PaymentData data) {

        DataResponse body = bankingRestClient.post()
                .uri(properties.getNearestPath())
                .contentType(MediaType.APPLICATION_JSON)
                .body(data)
                .retrieve()
                .onStatus(org.springframework.http.HttpStatusCode::isError,
                        (org.springframework.http.HttpRequest request, org.springframework.http.client.ClientHttpResponse response) -> {
                            org.springframework.http.HttpStatusCode status = response.getStatusCode();
                            if (status.is5xxServerError()) {
                                throw new RestClientRetryableException(
                                        "%s returned %s status code".formatted(SERVICE_NAME, status)
                                );
                            } else {
                                throw new RestClientNonRetryableException(
                                        "Client error %s when calling %s".formatted(status, SERVICE_NAME)
                                );
                            }
                        })
                .body(DataResponse.class);

        Assert.notNull(body, "%s returned null body".formatted(SERVICE_NAME));
        log.info("{} returned {}", SERVICE_NAME, maskIfNeeded("body", body));
        return body;

    }
}
