package com.example.walletservice.properties;

import com.example.walletservice.properties.RestClientProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "usd-client")
public class UsdClientProperties {
    private String nearestPath;
    private RestClientProperties config;
}
