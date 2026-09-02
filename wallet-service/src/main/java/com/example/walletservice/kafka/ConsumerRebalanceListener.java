package com.example.walletservice.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;

import java.util.Collection;

@Slf4j
public record ConsumerRebalanceListener(String clientId,
                                        String groupId) implements org.apache.kafka.clients.consumer.ConsumerRebalanceListener {
    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> collection) {
        log.info("Kafka rebalance. Client-id: {}, group-id: {}. {} partitions revoked.", clientId, groupId, collection);
    }

    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> collection) {

        log.info("Kafka rebalance. Client-id: {}, group-id: {}. {} partitions assigned.", clientId, groupId, collection);
    }
}
