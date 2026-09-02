package com.example.walletservice;

import com.example.walletservice.client.usd.UsdClientImpl;
import com.example.walletservice.client.usd.response.ResponseUsd;
import com.example.walletservice.service.ApiExchangeSerivce;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@SpringBootTest
class WalletServiceApplicationTests {

    @Autowired
    ApiExchangeSerivce cbRfService;

    @Autowired
    UsdClientImpl usdClient;

    @Test
    void contextLoads() {
    }

    @Test
    void getCourse() {
        Double course = cbRfService.convertUsdToRub(100.00);
        System.out.println(course);
    }

    @Test
    void getUsd() {
        String url = "https://www.cbr-xml-daily.ru/daily_json.js";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response.body());

            // Получаем курс доллара
            JsonNode usdNode = rootNode.path("Valute").path("USD");
            String usdName = usdNode.path("Name").asText();
            double usdRate = usdNode.path("Value").asDouble();

            System.out.println("Валюта: " + usdName);
            System.out.println("Курс ЦБ РФ: " + usdRate + " RUB");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void getCourseService() {

      ResponseUsd responseUsd = usdClient.getCourse();
      System.out.println(responseUsd);
    }





}
