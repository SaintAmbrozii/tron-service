package com.example.bankingservice.config;

import lombok.Data;
import org.apache.commons.lang3.function.Consumers;
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
    private Producers producers = new Producers();
    private BackoffConfig backoff = new BackoffConfig();

    @Data
    public static class Consumers {

        private ConsumerConfig bankingGroup = new ConsumerConfig();
    }

    @Data
    public static class Producers {
        private ProducerConfig bankingNotification = new ProducerConfig();
        private ProducerConfig userNotification = new ProducerConfig();
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
    public static class ProducerConfig {
        private String topic;
        private Duration syncTimeout;
    }

    @Data
    public static class BackoffConfig {
        private Duration interval;
        private int maxAttempts;
    }

    @Data
    public static class Topics {
        private TopicConfig bankingNotification = new TopicConfig();
        private TopicConfig userNotification = new TopicConfig();
    }

    @Data
    public static class TopicConfig {
        private String name;
        private int replicationCount;
        private int partitionsCount;
        private Duration timeoutOnProduce;
    }
}
