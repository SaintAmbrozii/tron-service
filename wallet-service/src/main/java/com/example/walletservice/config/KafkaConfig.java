package com.example.walletservice.config;

import lombok.Data;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "kafka-properties")
public class KafkaConfig {

    private Topics topics = new Topics();
    private Consumers consumers = new Consumers();
    private BackoffConfig backoff = new BackoffConfig();

    @Data
    public static class Topics {
        private TopicConfig bankingTopic = new TopicConfig();
    }

    @Data
    public static class TopicConfig {
        private String name;
        private int replicationCount;
        private int partitionsCount;
        private Duration timeoutOnProduce;
    }

    @Data
    public static class Consumers {

        private ConsumerConfig walletGroup = new ConsumerConfig();
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
