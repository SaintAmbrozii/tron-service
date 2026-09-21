package com.example.walletservice.service;

import com.example.walletservice.client.UsdClient;
import com.example.walletservice.client.usd.response.ResponseUsd;
import org.springframework.stereotype.Service;

@Service
public class ApiExchangeService {

    private final UsdClient usdClient;

    public ApiExchangeService(UsdClient usdClient) {
        this.usdClient = usdClient;
    }

    public ResponseUsd getCourse() {
         return usdClient.getCourse();
    }

    public Double convertUsdToRub(double amount) {
        return usdClient.getCourse().getValue() * amount;
    }

    public Double convertRubToUsd (double amount) {
        return usdClient.getCourse().getValue() / amount;
    }
}
