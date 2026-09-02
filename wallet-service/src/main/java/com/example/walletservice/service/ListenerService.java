package com.example.walletservice.service;


import com.example.avro.outbox.PaymentDataEvent;
import com.example.walletservice.config.KafkaConfig;
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
'MANUAL'.equals('${kafka-properties.consumers.wallet-group.ack-mode:}') 
  || 'MANUAL_IMMEDIATE'.equals('${kafka-properties.consumers.wallet-group.ack-mode:}')""")
public class ListenerService {

    private final UserExchangeService exchangeService;
    private final KafkaConfig kafkaConfig;


    public ListenerService(UserExchangeService exchangeService, KafkaConfig kafkaConfig) {
        this.exchangeService = exchangeService;
        this.kafkaConfig = kafkaConfig;
    }


    @KafkaListener(topics = "${kafka-properties.consumers.wallet-group.topic}",
            groupId = "${kafka-properties.consumers.wallet-group.group-id}",
            concurrency = "${kafka-properties.consumers.wallet-group.concurrency}",
            containerFactory = "walletListenerContainerFactory")
    public void KafkaListener(PaymentDataEvent event, @Header(KafkaHeaders.RECEIVED_KEY) String key,
                              @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
                              @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                              Acknowledgment acknowledgment) {
        log.info("Consuming message from Kafka: {}. Key: {}. Partition: {}. Topic: {}",
                event, key, partition, topic);
        log.info("In manual commit mode. Received OutboxBankingEventV1 {}", event);
        try {

            UUID uuid = event.getAggregateId();
            if (uuid != null) {
                // Передаем UUID напрямую в бизнес-логику без конвертаций
                exchangeService.updateExchande(uuid);
                // Фиксируем смещение (Offset) в Kafka
                acknowledgment.acknowledge();
                log.info("Successfully processed PaymentDataEvent for UUID: {}. Offset acknowledged.", uuid);

            } else {
                log.error("Received PaymentDataEvent with null aggregateId. Message will be acknowledged to avoid blockage.");
                acknowledgment.acknowledge();
            }

        } catch (Exception e) {
            log.error("Failed to handle OutboxBankingEventV1 {}", event, e);
            acknowledgment.nack(kafkaConfig.getConsumers().getWalletGroup().getNackDuration());
        }


    }

}
