package com.example.bankingservice.kafka;

import com.example.avro.outbox.PaymentDataEvent;
import com.example.bankingservice.config.KafkaConfig;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
    NewTopic bankingNotification() {
        return TopicBuilder
                .name(appKafkaProperties.getTopics().getBankingNotification().getName())
                .replicas(appKafkaProperties.getTopics().getBankingNotification().getReplicationCount())
                .partitions(appKafkaProperties.getTopics().getBankingNotification().getPartitionsCount())
                .build();
    }

    @Bean
    NewTopic userNotification() {
        return TopicBuilder
                .name(appKafkaProperties.getTopics().getUserNotification().getName())
                .replicas(appKafkaProperties.getTopics().getUserNotification().getReplicationCount())
                .partitions(appKafkaProperties.getTopics().getUserNotification().getPartitionsCount())
                .build();
    }

    @Bean
    NewTopic bankingGroupDltTopic() {
        // Берем имя оригинального топика из конфига консюмера
        String originalTopic = appKafkaProperties.getConsumers().getBankingGroup().getTopic();

        return TopicBuilder
                .name(originalTopic + ".DLT") // Имя будет: outbox_banking_event_v1.DLT
                .partitions(1) // Для DLT обычно достаточно 1 партиции, чтобы сохранять строгий порядок ошибок
                .replicas(1)   // Выставите количество реплик в соответствии с вашим окружением (в проде обычно 3)
                .build();
    }

    @Bean
    KafkaTemplate<String, Object> dltKafkaTemplate() {

        Map<String, Object> dltConfig = kafkaProperties.buildProducerProperties();

        dltConfig.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringSerializer.class);
        dltConfig.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringSerializer.class);

        ProducerFactory<String, Object> factory = new DefaultKafkaProducerFactory<>(dltConfig);
        factory.addListener(new MicrometerProducerListener<>(meterRegistry));

        KafkaTemplate<String, Object> template = new KafkaTemplate<>(factory);
        template.setObservationEnabled(true);

        return template;
    }


    @Bean
    @Primary
    KafkaTemplate<String, PaymentDataEvent> notificationEventTemplate() {
        ProducerFactory<String, PaymentDataEvent> producerFactory = defaultProducerFactory(meterRegistry);
        KafkaTemplate<String, PaymentDataEvent> template = new KafkaTemplate<>(producerFactory);
        template.setObservationEnabled(true);
        template.setProducerInterceptor(new LoggingKafkaProducerInterceptor<>());
        return template;
    }

    private <T> ProducerFactory<String, T> defaultProducerFactory(MeterRegistry meterRegistry) {

        Map<String, Object> config = kafkaProperties.buildProducerProperties();

        config.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringSerializer.class);

        config.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                io.confluent.kafka.serializers.KafkaAvroSerializer.class);

        config.put("schema.registry.url", "http://localhost:8081");
        config.put(org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG, "all");
        config.put(org.apache.kafka.clients.producer.ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        ProducerFactory<String, T> factory = new DefaultKafkaProducerFactory<>(config);
        factory.addListener(new MicrometerProducerListener<>(meterRegistry));

        return factory;
    }
}
