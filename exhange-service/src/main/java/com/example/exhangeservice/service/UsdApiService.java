package com.example.exhangeservice.service;

import com.example.exhangeservice.dto.ResponseUsd;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@Service
public class UsdApiService {

    private final String cb_uri = "https://www.cbr-xml-daily.ru/daily_json.js";


    public ResponseUsd getUsdCourse() {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(cb_uri)).build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Ошибка HTTP: " + response.statusCode());
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response.body());

            JsonNode usdNode = rootNode.path("Valute").path("USD");

            String usdName = usdNode.path("Name").asText();
            double usdRate = usdNode.path("Value").asDouble();

            if (usdName.isEmpty() || usdRate == 0.0) {
                log.warn("Не удалось распарсить данные USD из JSON");
            }

            return ResponseUsd.builder().value(usdRate).build();

        } catch (Exception e) {
            log.error("Ошибка при получении курса валют", e);
        } throw new RuntimeException("Ошибка при получении курса");
    }

}
