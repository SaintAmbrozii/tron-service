package com.example.walletservice.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "user-client")
public class UserClientProperties {
    private String nearestPath;
    private RestClientProperties config;

}
