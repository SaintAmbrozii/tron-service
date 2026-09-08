package com.example.walletservice.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "banking-client")
public class BankingClientProperties {
    private String nearestPath;
    private RestClientProperties config;
}
