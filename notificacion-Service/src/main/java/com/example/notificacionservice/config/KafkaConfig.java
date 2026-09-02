package com.example.notificacionservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "kafka-properties")
public class KafkaConfig {

    private Consumers consumers = new Consumers();
    private BackoffConfig backoff = new BackoffConfig();

    @Data
    public static class Consumers {

        private ConsumerConfig notificationGroup = new ConsumerConfig();
    }

    @Data
    public static class ConsumerConfig {
        private String topic;
        private ContainerProperties.AckMode ackMode;
        private int concurrency;
        private String groupId;
        private Duration nackDuration;
    }

    @Data
    public static class BackoffConfig {
        private Duration interval;
        private int maxAttempts;
    }

}
