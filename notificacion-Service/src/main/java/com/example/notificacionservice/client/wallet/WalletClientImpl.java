package com.example.notificacionservice.client.wallet;

import com.example.notificacionservice.client.AbstractClient;
import com.example.notificacionservice.client.wallet.response.ExchangeDto;
import com.example.notificacionservice.properties.WalletClientProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static com.example.notificacionservice.utils.MaskingUtil.maskIfNeeded;

@Slf4j
@Component
public class WalletClientImpl extends AbstractClient implements WalletClient {

    private static final String WALLET_BACKEND = "walletBackend";
    private static final String SERVICE_NAME = "wallet-service";
    private final WalletClientProperties properties;
    private final RestClient walletRestClient;

    public WalletClientImpl(@Autowired(required = false) ObjectMapper objectMapper,WalletClientProperties properties, RestClient walletRestClient) {
        super(objectMapper);
        this.properties = properties;
        this.walletRestClient = walletRestClient;
    }

    @CircuitBreaker(name = WALLET_BACKEND)
    @Retry(name = WALLET_BACKEND)
    @Override
    public ExchangeDto getExchange(UUID id) {
        ExchangeDto data = walletRestClient.get()
                .uri(properties.getNearestPath(),id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(org.springframework.http.HttpStatusCode::isError,
                        (org.springframework.http.HttpRequest request, org.springframework.http.client.ClientHttpResponse response) -> {
                            org.springframework.http.HttpStatusCode status = response.getStatusCode();
                            if (status.is5xxServerError()) {
                                throw new com.example.notificacionservice.exception.RestClientRetryableException(
                                        "%s returned %s status code".formatted(SERVICE_NAME, status)
                                );
                            } else {
                                throw new com.example.notificacionservice.exception.RestClientNonRetryableException(
                                        "Client error %s when calling %s".formatted(status, SERVICE_NAME)
                                );
                            }
                        })
                .body(ExchangeDto.class);

        Assert.notNull(data, "%s returned null body".formatted(SERVICE_NAME));
        log.info("{} returned {}", SERVICE_NAME, maskIfNeeded("body", data));
        return data;

    }
}
