package com.example.notificacionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


@ConfigurationPropertiesScan
@SpringBootApplication
public class NotificacionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificacionServiceApplication.class, args);
    }

}
