package com.example.walletservice.client.rest;

import com.example.walletservice.properties.RestClientProperties;
import org.springframework.web.client.RestClient;

public interface RestClientFactory {

    RestClient create(RestClientProperties properties);
}
