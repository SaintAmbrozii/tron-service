package com.example.notificacionservice.client.rest;

import com.example.notificacionservice.properties.RestClientProperties;
import org.springframework.web.client.RestClient;

public interface RestClientFactory {

    RestClient create(RestClientProperties properties);
}
