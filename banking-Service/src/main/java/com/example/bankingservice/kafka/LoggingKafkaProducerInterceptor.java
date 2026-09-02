package com.example.bankingservice.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.checkerframework.checker.units.qual.K;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class LoggingKafkaProducerInterceptor<K,V> implements ProducerInterceptor<K,V> {

    @Override
    public ProducerRecord<K, V> onSend(ProducerRecord<K, V> producerRecord) {
        // Безопасное извлечение заголовков (проверка на null не нужна, headers() никогда не null, но массив может быть пуст)
        String headersStr = Arrays.stream(producerRecord.headers().toArray())
                .map(header -> header.key() + "=" + (header.value() != null ? new String(header.value(), StandardCharsets.UTF_8) : "null"))
                .collect(Collectors.joining(", ", "[", "]"));

        log.info("Kafka producer: topic {}, partition {}, key {}. headers: {}",
                producerRecord.topic(),
                producerRecord.partition() != null ? producerRecord.partition() : "auto",
                producerRecord.key(),
                headersStr);

        // ЗАЩИТА ОТ NPE: Используем Objects.toString() или условие, вместо прямого .toString()
        log.info("Kafka producer: record {}", Objects.toString(producerRecord.value(), "null"));

        return producerRecord;
    }

    @Override
    public void onAcknowledgement(RecordMetadata recordMetadata, Exception e) {
        // Рекомендуется логировать ошибки отправки прямо здесь
        if (e != null) {
            log.error("Kafka producer failed to send message to topic {}",
                    recordMetadata != null ? recordMetadata.topic() : "unknown", e);
        }
    }

    @Override
    public void close() {}

    @Override
    public void configure(Map<String, ?> map) {}


}
