package com.example.walletservice.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class LoggingKafkaProducerInterceptor implements ProducerInterceptor<Object,Object> {


    @Override
    public ProducerRecord<Object, Object> onSend(ProducerRecord<Object, Object> producerRecord) {
        log.info("Kafka producer: topic {}, partition {}, key {}. headers: {}",
                producerRecord.topic(), producerRecord.partition(), producerRecord.key(),
                Arrays.stream(producerRecord.headers().toArray())
                        .map(header -> header.key() + "=" + new String(header.value(), StandardCharsets.UTF_8))
                        .collect(Collectors.joining(", ", "[", "]")));
        log.info("Kafka producer: record {}", producerRecord.value().toString());
        return producerRecord;
    }

    @Override
    public void onAcknowledgement(RecordMetadata recordMetadata, Exception e) {

    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> map) {

    }
}
