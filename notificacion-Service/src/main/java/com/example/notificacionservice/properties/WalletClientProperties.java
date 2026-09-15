package com.example.notificacionservice.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "wallet-client")
public class WalletClientProperties {
    private String nearestPath;
    private RestClientProperties config;

}
