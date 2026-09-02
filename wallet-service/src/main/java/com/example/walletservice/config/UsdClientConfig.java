package com.example.walletservice.config;

import com.example.walletservice.client.rest.RestClientFactory;
import com.example.walletservice.properties.UsdClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class UsdClientConfig {

    @Bean
    RestClient usdRestClient(UsdClientProperties usdClientProperties,
                             RestClientFactory restClientFactory) {
        return restClientFactory.create(usdClientProperties.getConfig());
    }

}
