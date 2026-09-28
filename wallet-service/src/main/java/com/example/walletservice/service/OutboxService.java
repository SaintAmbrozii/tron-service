package com.example.walletservice.service;

import com.example.avro.outbox.OutboxDataEvent;
import com.example.walletservice.config.KafkaConfig;
import com.example.walletservice.domain.Outbox;
import com.example.walletservice.repo.OutboxRepo;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OutboxService {

    private final OutboxRepo outboxRepo;
    private final KafkaTemplate<String, OutboxDataEvent> outboxDataEventKafkaTemplate;
    private final String applicationName;
    private final KafkaConfig kafkaConfig;

    public OutboxService(OutboxRepo outboxRepo, KafkaTemplate<String, OutboxDataEvent> outboxDataEventKafkaTemplate,
                         @Value("${spring.application.name}")String applicationName, KafkaConfig kafkaConfig) {
        this.outboxRepo = outboxRepo;
        this.outboxDataEventKafkaTemplate = outboxDataEventKafkaTemplate;
        this.applicationName = applicationName;
        this.kafkaConfig = kafkaConfig;
    }

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void pollAndSend() {

        KafkaConfig.TopicConfig topicConfig = kafkaConfig.getTopics().getBankingTopic();

        List<Outbox> outboxList = outboxRepo.findAllByStatusIsFalse();
        if (!outboxList.isEmpty()) {
            List<UUID> idsToDelete = new ArrayList<>();

            for (Outbox outbox: outboxList) {

                OutboxDataEvent outboxDataEvent = OutboxDataEvent.newBuilder()
                        .setId(outbox.getId())
                        .setUserId(outbox.getUserId())
                        .setAmount(String.valueOf(outbox.getAmount()))
                        .setPhone(outbox.getPhone())
                        .setAggregateId(outbox.getAggregateId()).build();
                ProducerRecord<String, OutboxDataEvent> record = new ProducerRecord<>(
                        topicConfig.getName(),
                        outbox.getAggregateId().toString(),
                        outboxDataEvent);

                try {
                    outboxDataEventKafkaTemplate.send(record).get(topicConfig.getTimeoutOnProduce().toMillis(), TimeUnit.MILLISECONDS);

                    log.info("Successfully sent event {} to kafka.", outboxDataEvent);

                    idsToDelete.add(outbox.getId());

                }catch (Exception e) {

                    log.error("[OUTBOX-ШЕДУЛЕР] Сбой отправки отложенного сообщения {} в Kafka. " +
                            "Оно остается в БД до следующего цикла. Причина: {}", outbox.getId(), e.getMessage());
                }

            }

            if (!idsToDelete.isEmpty()) {
                outboxRepo.deleteByIdsInBatch(idsToDelete);
                log.info("[OUTBOX] Пакетно удалено {} записей из БД.", idsToDelete.size());
            }

        }

    }
}
