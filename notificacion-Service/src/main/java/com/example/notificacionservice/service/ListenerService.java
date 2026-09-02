package com.example.notificacionservice.service;


import com.example.avro.outbox.PaymentDataEvent;
import com.example.notificacionservice.config.KafkaConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Slf4j
@Service
@ConditionalOnExpression("""
'MANUAL'.equals('${kafka-properties.consumers.notification-group.ack-mode:}') 
  || 'MANUAL_IMMEDIATE'.equals('${kafka-properties.consumers.notification-group.ack-mode:}')""")
public class ListenerService {


    private final KafkaConfig kafkaConfig;
    private final NotificationService notificationService;


    public ListenerService(KafkaConfig kafkaConfig, NotificationService notificationService) {
        this.kafkaConfig = kafkaConfig;
        this.notificationService = notificationService;
    }


    @KafkaListener(topics = "${kafka-properties.consumers.notification-group.topic}",
            groupId = "${kafka-properties.consumers.notification-group.group-id}",
            concurrency = "${kafka-properties.consumers.notification-group.concurrency}",
            containerFactory = "walletListenerContainerFactory")
    public void KafkaListener(PaymentDataEvent event, @Header(KafkaHeaders.RECEIVED_KEY) String key,
                              @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
                              @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                              Acknowledgment acknowledgment) {
        log.info("Consuming message from Kafka: {}. Key: {}. Partition: {}. Topic: {}",
                event, key, partition, topic);
        log.info("In manual commit mode. Received OutboxBankingEventV1 {}", event);
        try {

            System.out.println(event);

           notificationService.sendNotification(event);

           acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to handle OutboxBankingEventV1 {}", event, e);
            acknowledgment.nack(kafkaConfig.getConsumers().getNotificationGroup().getNackDuration());
        }


    }

}
