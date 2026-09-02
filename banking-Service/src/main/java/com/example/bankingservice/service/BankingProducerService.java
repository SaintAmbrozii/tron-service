package com.example.bankingservice.service;

import com.example.avro.outbox.OutboxDataEvent;
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
public class BankingProducerService {

    private final KafkaTemplate<String, PaymentDataEvent> paymentDataEventKafkaTemplate;
    private final String applicationName;
    private final KafkaConfig kafkaConfig;

    public BankingProducerService(KafkaTemplate<String, PaymentDataEvent> paymentDataEventKafkaTemplate,
                                  @Value("${spring.application.name}") String applicationName, KafkaConfig kafkaConfig) {
        this.paymentDataEventKafkaTemplate = paymentDataEventKafkaTemplate;
        this.applicationName = applicationName;
        this.kafkaConfig = kafkaConfig;
    }


    public void sendNotification(UUID uuid,String userid){

        KafkaConfig.TopicConfig topicConfig = kafkaConfig.getTopics().getBankingNotification();

        PaymentDataEvent event = PaymentDataEvent.newBuilder()
                .setUserId(userid).setAggregateId(uuid).build();


        ProducerRecord<String, PaymentDataEvent> record = new ProducerRecord<>(
                topicConfig.getName(),
                event.getUserId(),
                event);
        System.out.println(record);
        try {
            paymentDataEventKafkaTemplate.send(record).get(topicConfig.getTimeoutOnProduce().toMillis(), TimeUnit.MILLISECONDS);

            log.info("Successfully sent event {} to kafka.", event);

        }catch (Exception e) {
            log.error("КОНКРЕТНЫЙ КЛАСС ИСКЛЮЧЕНИЯ КАФКИ: {}", e.getCause() != null ? e.getCause().getClass().getName() : e.getClass().getName());
            log.error("ТЕКСТ ОШИБКИ: {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.err.println("!!! КРИТИЧЕСКИЙ СБОЙ ДВИЖКА КАФКИ МЕЖДУ СЕРВИСАМИ !!!");

            // Вытаскиваем КЛАСС оригинальной ошибки (например, TimeoutException, ConnectException)
            if (e.getCause() != null) {
                System.err.println("КЛАСС КОРНЕВОЙ ОШИБКИ: " + e.getCause().getClass().getName());
                System.err.println("СООБЩЕНИЕ ОШИБКИ: " + e.getCause().getMessage());
            } else {
                System.err.println("КЛАСС ОШИБКИ: " + e.getClass().getName());
                System.err.println("СООБЩЕНИЕ ОШИБКИ: " + e.getMessage());
            }
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

            // Печатаем ПОЛНЫЙ стек-трейс, чтобы увидеть, какая именно строка внутри библиотек упала
            e.printStackTrace();

            throw new RuntimeException("Сбой отправки сообщений в кафку");
        }

    }
}
