package com.example.notificacionservice.client.wallet;

import com.example.notificacionservice.client.AbstractClient;
import com.example.notificacionservice.client.wallet.response.ExchangeDto;
import com.example.notificacionservice.properties.WalletClientProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

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
        return
                null;
    }
}
