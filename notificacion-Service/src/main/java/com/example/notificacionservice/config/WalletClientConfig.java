package com.example.notificacionservice.config;

import com.example.notificacionservice.client.rest.RestClientFactory;
import com.example.notificacionservice.properties.UserClientProperties;
import com.example.notificacionservice.properties.WalletClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class WalletClientConfig {

    @Bean
    RestClient walletRestClient(WalletClientProperties walletClientProperties,
                             RestClientFactory restClientFactory) {
        return restClientFactory.create(walletClientProperties.getConfig());
    }
}
