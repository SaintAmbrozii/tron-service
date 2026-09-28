package com.example.walletservice.config;


import com.example.walletservice.client.rest.RestClientFactory;
import com.example.walletservice.properties.UserClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class UserClientConfig {

    @Bean
    RestClient userRestClient(UserClientProperties userClientProperties,
                              RestClientFactory restClientFactory) {
        return restClientFactory.create(userClientProperties.getConfig());
    }
}
