package com.example.walletservice.event;

import com.example.walletservice.service.ProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class BankingTransactionListener {

    private final ProducerService producerService;

    public BankingTransactionListener(ProducerService producerService) {
        this.producerService = producerService;
    }


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApplicationEvent(OutboxEvent event) {

        producerService.sendEvent(event);

    }


}
