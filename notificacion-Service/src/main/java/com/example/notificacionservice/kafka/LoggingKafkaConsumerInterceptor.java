package com.example.notificacionservice.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.RecordInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
public class LoggingKafkaConsumerInterceptor<K,V> implements RecordInterceptor<K,V> {

    @Override
    public ConsumerRecord<K, V> intercept(ConsumerRecord<K, V> record, Consumer<K, V> consumer) {
        log.info("Kafka consumer: topic {}, partition {}, key {}. headers: {}",
                record.topic(), record.partition(), record.key(),
                Arrays.stream(record.headers().toArray())
                        .map(header -> header.key() + "=" + new String(header.value(), StandardCharsets.UTF_8))
                        .collect(Collectors.joining(", ", "[", "]")));
        log.info("Kafka consumer: record {}", record.value());
        return record;

    }
}
