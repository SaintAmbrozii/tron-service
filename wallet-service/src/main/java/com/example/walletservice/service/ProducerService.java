package com.example.walletservice.service;

import com.example.avro.outbox.OutboxDataEvent;
import com.example.walletservice.config.KafkaConfig;
import com.example.walletservice.domain.Outbox;
import com.example.walletservice.event.OutboxEvent;
import com.example.walletservice.repo.OutboxRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ProducerService {

    private final OutboxRepo outboxRepo;
    private final KafkaTemplate<String, OutboxDataEvent> outboxDataEventKafkaTemplate;
    private final KafkaConfig kafkaConfig;


    public ProducerService(OutboxRepo outboxRepo, KafkaTemplate<String, OutboxDataEvent> outboxDataEventKafkaTemplate, KafkaConfig kafkaConfig) {
        this.outboxRepo = outboxRepo;
        this.outboxDataEventKafkaTemplate = outboxDataEventKafkaTemplate;
        this.kafkaConfig = kafkaConfig;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendEvent(OutboxEvent event) {
        KafkaConfig.TopicConfig topicConfig = kafkaConfig.getTopics().getBankingTopic();

        Optional<Outbox> outbox = outboxRepo.findById(event.outboxId());

        if (outbox.isPresent()) {

            Outbox inDb = outbox.get();

            OutboxDataEvent outboxDataEvent = OutboxDataEvent.newBuilder()
                    .setId(outbox.get().getId())
                    .setUserId(outbox.get().getUserId())
                    .setAmount(String.valueOf(outbox.get().getAmount()))
                    .setPhone(outbox.get().getPhone())
                    .setAggregateId(outbox.get().getAggregateId()).build();
            ProducerRecord<String, OutboxDataEvent> record = new ProducerRecord<>(
                    topicConfig.getName(),
                    outbox.get().getUserId(),
                    outboxDataEvent);
            try {
                outboxDataEventKafkaTemplate.send(record).get(topicConfig.getTimeoutOnProduce().toMillis(), TimeUnit.MILLISECONDS);

                outboxRepo.deleteById(inDb.getId());
                log.info("Successfully sent event {} to kafka.", outboxDataEvent);


            }catch (Exception e) {
                log.error("Failed to send outbox {} to Kafka: {}", outboxDataEvent, e.getMessage());
                throw new RuntimeException("Kafka produce failed, keeping outbox record in DB", e);
            }

        }

    }
}
