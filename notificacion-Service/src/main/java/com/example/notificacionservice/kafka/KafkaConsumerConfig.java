package com.example.notificacionservice.kafka;


import com.example.avro.outbox.PaymentDataEvent;
import com.example.notificacionservice.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final KafkaConfig appKafkaProperties;
    private final KafkaProperties kafkaProperties;

    @Bean
    public KafkaTemplate<Object, Object> rawDltKafkaTemplate() {
        Map<String, Object> props = new HashMap<>();
        props.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // Используем ByteArraySerializer вместо KafkaAvroSerializer для безопасности DLT
        props.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);

        ProducerFactory<Object, Object> factory = new DefaultKafkaProducerFactory<>(props);
        return new KafkaTemplate<>(factory);
    }




    @Bean
    CommonErrorHandler commonErrorHandler(KafkaTemplate<Object, Object> rawDltKafkaTemplate) {
        // Передаем наш безопасный сырой шаблон в recoverer
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(rawDltKafkaTemplate) {
            @Override
            public void accept(ConsumerRecord<?, ?> record, Exception exception) {
                if (record == null) {
                    log.error("Failed to process message. Record is null.");
                    return;
                }
                boolean isDeserializationError = isDeserializationException(exception);
                if (isDeserializationError) {
                    log.error("POISON PILL DETECTED! Failed to deserialize message in topic {} at partition {} offset {}. Sending raw bytes to DLT.",
                            record.topic(), record.partition(), record.offset());
                } else {
                    log.error("Business error or timeout in topic {} at offset {}. Max attempts reached. Sending to DLT. Exception: {}",
                            record.topic(), record.offset(), exception.getMessage());
                }
                super.accept(record, exception);
            }
        };

        BackOff backOff = new FixedBackOff(
                appKafkaProperties.getBackoff().getInterval().toMillis(),
                appKafkaProperties.getBackoff().getMaxAttempts()
        );

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        errorHandler.addNotRetryableExceptions(
                NullPointerException.class,
                org.apache.kafka.common.errors.SerializationException.class,
                org.springframework.kafka.support.serializer.DeserializationException.class
        );

        return errorHandler;
    }

    private boolean isDeserializationException(Throwable ex) {
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof org.apache.kafka.common.errors.SerializationException
                    || cause instanceof org.springframework.kafka.support.serializer.DeserializationException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String,PaymentDataEvent> walletListenerContainerFactory(
            CommonErrorHandler commonErrorHandler) {
        KafkaConfig.ConsumerConfig consumerConfig = appKafkaProperties.getConsumers().getNotificationGroup();
        ConsumerFactory<String, PaymentDataEvent> consumerFactory = getConsumerFactory(consumerConfig);

        return getConcurrentKafkaListenerContainerFactory(consumerFactory, commonErrorHandler, consumerConfig);
   }

    private <K, V extends SpecificRecordBase> ConsumerFactory<K, V> getConsumerFactory(
            KafkaConfig.ConsumerConfig appConsumerConfig) {
        Map<String, Object> config = getConsumerConfig(appConsumerConfig);
        return new DefaultKafkaConsumerFactory<>(config);
    }

    private Map<String, Object> getConsumerConfig(KafkaConfig.ConsumerConfig appConsumerConfig) {
        Map<String, Object> config = kafkaProperties.buildConsumerProperties();

        config.put(ConsumerConfig.CLIENT_ID_CONFIG, "%s-%s".formatted(appConsumerConfig.getTopic(), UUID.randomUUID().toString()));
        config.put(ConsumerConfig.GROUP_ID_CONFIG, appConsumerConfig.getGroupId());

        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, io.confluent.kafka.serializers.KafkaAvroDeserializer.class);

        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        config.put("schema.registry.url", "http://localhost:8081");
        config.put("specific.avro.reader", "true");
        config.put("avro.use.logical.type.converters", "true");

        return config;
    }

    private <K, V extends SpecificRecordBase> ConcurrentKafkaListenerContainerFactory<K, V> getConcurrentKafkaListenerContainerFactory(
            ConsumerFactory<K, V> consumerFactory,
            CommonErrorHandler commonErrorHandler,
            KafkaConfig.ConsumerConfig consumerConfig
    ) {
        ConcurrentKafkaListenerContainerFactory<K, V> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(commonErrorHandler);
        factory.getContainerProperties().setObservationEnabled(true);
        factory.getContainerProperties().setAckMode(consumerConfig.getAckMode());
        factory.setRecordInterceptor(new LoggingKafkaConsumerInterceptor<>());
        factory.setConcurrency(consumerConfig.getConcurrency());
        Object clientId = consumerFactory.getConfigurationProperties().get(ConsumerConfig.CLIENT_ID_CONFIG);
        Object groupId = consumerFactory.getConfigurationProperties().get(ConsumerConfig.GROUP_ID_CONFIG);
        if (Objects.nonNull(clientId) && Objects.nonNull(groupId)) {
            factory.getContainerProperties().setConsumerRebalanceListener(
                    new ConsumerRebalanceListener((String) clientId, (String) groupId)
            );
        }
        return factory;
    }


}
