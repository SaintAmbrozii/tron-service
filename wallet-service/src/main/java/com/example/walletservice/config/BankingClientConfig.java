package com.example.walletservice.config;

import com.example.walletservice.client.rest.RestClientFactory;
import com.example.walletservice.properties.BankingClientProperties;
import com.example.walletservice.properties.UserClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class BankingClientConfig {

    @Bean
    RestClient bankingRestClient(BankingClientProperties bankingClientProperties,
                              RestClientFactory restClientFactory) {
        return restClientFactory.create(bankingClientProperties.getConfig());
    }
}
