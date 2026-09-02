package com.example.walletservice.tronclient;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.tron.trident.core.ApiWrapper;

@Component
public class TronClient {

    private static final String owner_private_key = "4a6213c5c05dd3dc8793837dde88b74bd9a45b1b9dc7663f21b771bdd56ad50b";

    @Value("${tron.private-key}")
    private String privateKey;

    @Bean(destroyMethod = "close")
    public  ApiWrapper client() {
        return ApiWrapper.ofNile(privateKey);
    }


}
