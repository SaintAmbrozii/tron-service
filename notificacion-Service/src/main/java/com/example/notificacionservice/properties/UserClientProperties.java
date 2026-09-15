package com.example.notificacionservice.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "user-client")
public class UserClientProperties {
    private String nearestPath;
    private RestClientProperties config;

}
