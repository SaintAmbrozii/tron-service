package com.example.walletservice.kafka;

import com.example.avro.outbox.OutboxDataEvent;
import com.example.walletservice.config.KafkaConfig;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.MicrometerProducerListener;
import org.springframework.kafka.core.ProducerFactory;


import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class KafkaProducerConfig {

    private final KafkaConfig appKafkaProperties;
    private final KafkaProperties kafkaProperties;
    private final MeterRegistry meterRegistry;

    @Bean
    NewTopic walletGroupDltTopic() {
        // Берем имя оригинального топика из конфига консюмера
        String originalTopic = appKafkaProperties.getConsumers().getWalletGroup().getTopic();

        return TopicBuilder
                .name(originalTopic + ".DLT") // Имя будет: outbox_banking_event_v1.DLT
                .partitions(1) // Для DLT обычно достаточно 1 партиции, чтобы сохранять строгий порядок ошибок
                .replicas(1)   // Выставите количество реплик в соответствии с вашим окружением (в проде обычно 3)
                .build();
    }

    @Bean
    KafkaTemplate<String, Object> dltKafkaTemplate() {
        // Используем ту же фабрику, настроенную на чтение вашего yml (с поддержкой Avro/Schema Registry)
        ProducerFactory<String, Object> factory = defaultProducerFactory(meterRegistry);

        KafkaTemplate<String, Object> template = new KafkaTemplate<>(factory);
        template.setObservationEnabled(true);

        return template;
    }

    @Bean
    NewTopic bankingTopic() {
        return TopicBuilder
                .name(appKafkaProperties.getTopics().getBankingTopic().getName())
                .replicas(appKafkaProperties.getTopics().getBankingTopic().getReplicationCount())
                .partitions(appKafkaProperties.getTopics().getBankingTopic().getPartitionsCount())
                .build();
    }
    @Bean
    KafkaTemplate<String, OutboxDataEvent> outboxBankingEvent(ProducerFactory<String, OutboxDataEvent> outboxDataEventProducerFactory) {
        KafkaTemplate<String, OutboxDataEvent> template = new KafkaTemplate<>(outboxDataEventProducerFactory);
        template.setObservationEnabled(true);
        return template;
    }

    @Bean
    ProducerFactory<String, OutboxDataEvent> OutboxProducerFactory(MeterRegistry meterRegistry) {
        Map<String, Object> config = kafkaProperties.buildProducerProperties();


        config.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, LoggingKafkaProducerInterceptor.class.getName());

        ProducerFactory<String, OutboxDataEvent> factory =
                new DefaultKafkaProducerFactory<>(config);

        factory.addListener(new MicrometerProducerListener<>(meterRegistry));

        return factory;
    }

    private <T> ProducerFactory<String, T> defaultProducerFactory(MeterRegistry meterRegistry) {

        Map<String, Object> config = kafkaProperties.buildProducerProperties();

        ProducerFactory<String, T> factory =
                new DefaultKafkaProducerFactory<>(config);

        factory.addListener(new MicrometerProducerListener<>(meterRegistry));

        return factory;
    }
}
