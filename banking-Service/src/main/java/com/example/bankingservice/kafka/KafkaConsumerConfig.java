package com.example.bankingservice.kafka;

import com.example.avro.outbox.OutboxDataEvent;
import com.example.bankingservice.config.KafkaConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
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
    CommonErrorHandler commonErrorHandler(KafkaTemplate<String, Object> dltKafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(dltKafkaTemplate) {
            @Override
            public void accept(ConsumerRecord<?, ?> record, Exception exception) {
                if (record == null) {
                    log.error("Failed to process message. Record is null. Exception: {}", exception.getMessage(), exception);
                    return;
                }

                boolean isDeserializationError = isDeserializationException(exception);

                if (isDeserializationError) {
                    log.error("POISON PILL DETECTED! Failed to deserialize message in topic {} at partition {} offset {}. Sending to DLT.",
                            record.topic(), record.partition(), record.offset(), exception);

                } else {
                    log.error("Business error or timeout in topic {} at offset {}. Max attempts reached. Sending to DLT. Exception: {}",
                            record.topic(), record.offset(), exception.getMessage());
                }

                super.accept(record, exception);
            }
        };
        // 2. Настраиваем политику повторов (5 попыток по 5 секунд)
        BackOff backOff = new FixedBackOff(
                appKafkaProperties.getBackoff().getInterval().toMillis(),
                appKafkaProperties.getBackoff().getMaxAttempts()
        );

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
        // 3. Исключения, при которых НЕ нужно делать повторные попытки (сразу мгновенно отправляем в DLT)
        errorHandler.addNotRetryableExceptions(
                NullPointerException.class,
                org.apache.kafka.common.errors.SerializationException.class,
                org.springframework.kafka.support.serializer.DeserializationException.class
        );

        return errorHandler;
    }

    // Хелпер-метод для рекурсивного поиска причины ошибки десериализации
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
    ConcurrentKafkaListenerContainerFactory<String, OutboxDataEvent> kafkaListenerContainerFactory(
            CommonErrorHandler commonErrorHandler) {
        KafkaConfig.ConsumerConfig consumerConfig = appKafkaProperties.getConsumers().getBankingGroup();
        ConsumerFactory<String, OutboxDataEvent> consumerFactory = getConsumerFactory(consumerConfig);

        return getConcurrentKafkaListenerContainerFactory(consumerFactory, commonErrorHandler, consumerConfig);
    }
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // или из вашего конфига
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);

        // ДОБАВЬТЕ ЭТУ СТРОКУ, если её нет:
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Настройки Schema Registry для Avro
        props.put("schema.registry.url", "http://localhost:8081");
        props.put("specific.avro.reader", "true");

        return new DefaultKafkaConsumerFactory<>(props);
    }

    private <K, V extends SpecificRecordBase> ConsumerFactory<K, V> getConsumerFactory(
            KafkaConfig.ConsumerConfig appConsumerConfig) {
        Map<String, Object> config = getConsumerConfig(appConsumerConfig);
        return new DefaultKafkaConsumerFactory<>(config);
    }

    private Map<String, Object> getConsumerConfig(KafkaConfig.ConsumerConfig appConsumerConfig) {
        Map<String, Object> config = kafkaProperties.buildConsumerProperties();

        // 2. Идентификаторы группы и клиента
        config.put(ConsumerConfig.CLIENT_ID_CONFIG, "%s-%s".formatted(appConsumerConfig.getTopic(), UUID.randomUUID().toString()));
        config.put(ConsumerConfig.GROUP_ID_CONFIG, appConsumerConfig.getGroupId());

        // 3. ЯВНО задаем десериализаторы (String и Avro)
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, io.confluent.kafka.serializers.KafkaAvroDeserializer.class);

        // 4. ГАРАНТИРУЕМ чтение старых сообщений с самого начала для новых групп
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // 5. Обязательные настройки Confluent Schema Registry для чтения бинарного Avro
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
