package com.example.bankingservice.service;

import com.example.avro.outbox.PaymentDataEvent;
import com.example.bankingservice.config.KafkaConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserProducerService {

    private final KafkaTemplate<String, PaymentDataEvent> paymentDataEventKafkaTemplate;
    private final String applicationName;
    private final KafkaConfig kafkaConfig;

    public UserProducerService(KafkaTemplate<String, PaymentDataEvent> paymentDataEventKafkaTemplate,
                               @Value("${spring.application.name}") String applicationName, KafkaConfig kafkaConfig) {
        this.paymentDataEventKafkaTemplate = paymentDataEventKafkaTemplate;
        this.applicationName = applicationName;
        this.kafkaConfig = kafkaConfig;
    }


    public void sendUserNotification(UUID uuid,String userid){

        KafkaConfig.TopicConfig topicConfig = kafkaConfig.getTopics().getUserNotification();

        PaymentDataEvent event = PaymentDataEvent.newBuilder()
                .setUserId(userid).setAggregateId(uuid).build();

        ProducerRecord<String, PaymentDataEvent> record = new ProducerRecord<>(
                topicConfig.getName(),
                event.getUserId(),
                event);
        System.out.println(record);

        try {
            paymentDataEventKafkaTemplate.send(record).whenComplete((result, exception) -> {
                if (exception == null) {
                    log.info("Асинхронное уведомление успешно доставлено. Offset: {}",
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Ошибка асинхронной доставки в топик пользователей: {}", exception.getMessage());
                }
            });
            log.info("Successfully sent event {} to kafka.", event);

        }catch (Exception e) {
            log.error("КОНКРЕТНЫЙ КЛАСС ИСКЛЮЧЕНИЯ КАФКИ: {}", e.getCause() != null ? e.getCause().getClass().getName() : e.getClass().getName());
            log.error("ТЕКСТ ОШИБКИ: {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Сбой отправки сообщений в кафку");
        }

    }
}
