package com.example.bankingservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
public class BankingPollingService {

    private final ThreadPoolTaskScheduler taskScheduler;
    private final BankingService bankingService;

    public BankingPollingService(ThreadPoolTaskScheduler taskScheduler, @Lazy BankingService bankingService) {
        this.taskScheduler = taskScheduler;
        this.bankingService = bankingService;
    }


    public void startPolling(String qrId, UUID uuid) {

        Instant startTime = Instant.now();
        Duration timeoutLimit = Duration.ofMinutes(10);

        final ScheduledFuture<?>[] futureHolder = new ScheduledFuture<?>[1];

        Runnable pollingTask = new Runnable() {
            @Override
            public void run() {
                try {

                    if (Duration.between(startTime, Instant.now()).compareTo(timeoutLimit) > 0) {
                        futureHolder[0].cancel(false); // Останавливаем поллинг
                        return;
                    }

                    String status = bankingService.getStatus(qrId);

                    if (status.equals("Active")) {
                        bankingService.createPaymentAndTransfer(uuid);

                        futureHolder[0].cancel(false); // Останавливаем поллинг
                    }
                } catch (Exception e) {

                    log.info("Ошибка при операции обмена");
                }
            }
        };

        futureHolder[0] = taskScheduler.scheduleAtFixedRate(pollingTask, Duration.ofSeconds(3));
    }

}
