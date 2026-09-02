package com.example.walletservice.event;

import com.example.avro.outbox.OutboxDataEvent;
import com.example.walletservice.config.KafkaConfig;
import com.example.walletservice.domain.Outbox;
import com.example.walletservice.repo.OutboxRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class BankingTransactionListener implements ApplicationListener<OutboxEvent> {

    private final OutboxRepo outboxRepo;
    private final KafkaTemplate<String, OutboxDataEvent> outboxDataEventKafkaTemplate;
    private final String applicationName;
    private final KafkaConfig kafkaConfig;

    public BankingTransactionListener(OutboxRepo outboxRepo, KafkaTemplate<String, OutboxDataEvent> outboxDataEventKafkaTemplate,
                                      @Value("${spring.application.name}")String applicationName, KafkaConfig kafkaConfig) {
        this.outboxRepo = outboxRepo;
        this.outboxDataEventKafkaTemplate = outboxDataEventKafkaTemplate;
        this.applicationName = applicationName;
        this.kafkaConfig = kafkaConfig;
    }


    @Override
    public void onApplicationEvent(OutboxEvent event) {

        sendEvent(event);
    }

    public void sendEvent(OutboxEvent event) {
        KafkaConfig.TopicConfig topicConfig = kafkaConfig.getTopics().getBankingTopic();
        UUID eventId = event.getOutboxId();
        Optional<Outbox> outbox = outboxRepo.findById(eventId);

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
                throw new RuntimeException("Сбой отправки сообщений в кафку");
            }

        }

    }
}
