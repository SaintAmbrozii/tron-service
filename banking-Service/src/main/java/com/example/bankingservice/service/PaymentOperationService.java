package com.example.bankingservice.service;

import com.example.avro.outbox.OutboxDataEvent;
import com.example.bankingservice.domain.Payments;
import com.example.bankingservice.repo.PaymentsRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentOperationService {

    private final PaymentsRepo paymentsRepo;
    private final BankingProducerService producerService;
    private final UserProducerService userProducerService;

    public PaymentOperationService(PaymentsRepo paymentsRepo, BankingProducerService producerService,
                                   UserProducerService userProducerService) {
        this.paymentsRepo = paymentsRepo;
        this.producerService = producerService;
        this.userProducerService = userProducerService;
    }

    public void savePayment(OutboxDataEvent event) {
        Payments payments = Payments.builder().phone(event.getPhone()).aggregateId(event.getAggregateId())
                .amount(new BigDecimal(event.getAmount()))
                .userId(event.getUserId()).status(false).build();
        System.out.println(payments);
        Payments saved = paymentsRepo.save(payments);

        this.createPayment(saved.getId());
    }


    @Transactional
    public void createPayment(UUID uuid) {
        Optional<Payments> payments = paymentsRepo.findById(uuid);

        if (payments.isPresent()) {
            Payments update = payments.get();
            update.setStatus(true);
            paymentsRepo.save(update);

            UUID aggregateId = update.getAggregateId();
            String userId = update.getUserId();

            producerService.sendNotification(aggregateId, userId,"success");
            userProducerService.sendUserNotification(aggregateId,userId,"success");

        }
    }

    @Transactional
    public void createPaymentAndTransfer(UUID uuid) {
        Optional<Payments> payments = paymentsRepo.findById(uuid);

        if (payments.isPresent()) {
            Payments update = payments.get();
            update.setStatus(true);
            paymentsRepo.save(update);

            UUID aggregateId = update.getAggregateId();
            String userId = update.getUserId();

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    // Этот код выполнится строго после того, как БД скажет "ОК"
                    producerService.sendNotification(aggregateId, userId, "payment");
                    userProducerService.sendUserNotification(aggregateId, userId, "payment");
                }
            });

        }
    }

}
