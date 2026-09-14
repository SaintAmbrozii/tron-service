package com.example.walletservice.service;


import com.example.walletservice.client.usd.UsdClientImpl;
import com.example.walletservice.client.usd.response.ResponseUsd;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@Slf4j
public class ApiExchangeSerivce {

    private final String cb_uri = "https://www.cbr-xml-daily.ru/daily_json.js";
    private final UsdClientImpl usdClient;

    public ApiExchangeSerivce(UsdClientImpl usdClient) {
        this.usdClient = usdClient;
    }


    public ResponseUsd getUsdCourse() {

        return usdClient.getCourse();
    }

    public double convertUsdToRub(double amount) {
        return getUsdCourse().getValue() * amount;
    }

    public double convertRubToUsd(double amount) {
        return amount / getUsdCourse().getValue();
    }



}
