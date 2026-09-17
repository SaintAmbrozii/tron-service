package com.example.bankingservice.service;



import com.example.avro.outbox.OutboxDataEvent;
import com.example.bankingservice.config.KafkaConfig;
import com.example.bankingservice.domain.Payments;
import lombok.extern.slf4j.Slf4j;
import com.example.bankingservice.repo.PaymentsRepo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Slf4j
@Service
@ConditionalOnExpression("""
'MANUAL'.equals('${kafka-properties.consumers.banking-group.ack-mode:}') 
  || 'MANUAL_IMMEDIATE'.equals('${kafka-properties.consumers.banking-group.ack-mode:}')""")

public class ListenerService {


    private final KafkaConfig kafkaConfig;
    private final BankingService bankingService;

    public ListenerService(KafkaConfig kafkaConfig, BankingService bankingService) {
        this.kafkaConfig = kafkaConfig;
        this.bankingService = bankingService;
    }


    @KafkaListener(topics = "${kafka-properties.consumers.banking-group.topic}",
            groupId = "${kafka-properties.consumers.banking-group.group-id}",
            concurrency = "${kafka-properties.consumers.banking-group.concurrency}",
            containerFactory = "kafkaListenerContainerFactory")
    public void KafkaListener(OutboxDataEvent event, @Header(KafkaHeaders.RECEIVED_KEY) String key,
                              @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
                              @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                              Acknowledgment acknowledgment) {

        log.info("Consuming message from Kafka: {}. Key: {}. Partition: {}. Topic: {}",
                event, key, partition, topic);

        log.info("In manual commit mode. Received OutboxBankingEventV1 {}", event);
        try {

            bankingService.savePayment(event);
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to handle OutboxBankingEventV1 {}", event, e);
            acknowledgment.nack(kafkaConfig.getConsumers().getBankingGroup().getNackDuration());
        }


    }

}
