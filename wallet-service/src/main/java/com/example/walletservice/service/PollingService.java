package com.example.walletservice.service;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;

@Service
public class PollingService {

    private final WalletService walletService;
    private final UserExchangeService userExchangeService;
    private final ThreadPoolTaskScheduler taskScheduler;

    public PollingService(WalletService walletService, UserExchangeService userExchangeService, ThreadPoolTaskScheduler taskScheduler) {
        this.walletService = walletService;
        this.userExchangeService = userExchangeService;
        this.taskScheduler = taskScheduler;
    }

    public void startPolling(String userId,String walletAddress, String phone, BigDecimal rubAmount,
                             BigDecimal targetAmount) {

        Instant startTime = Instant.now();
        Duration timeoutLimit = Duration.ofMinutes(10);

        // Хранилище для ссылки на задачу, чтобы она могла отменить саму себя
        final ScheduledFuture<?>[] futureHolder = new ScheduledFuture<?>[1];

        Runnable pollingTask = new Runnable() {
            @Override
            public void run() {
                try {
                    // 1. Проверяем, не вышли ли за лимит 10 минут
                    if (Duration.between(startTime, Instant.now()).compareTo(timeoutLimit) > 0) {
                        futureHolder[0].cancel(false); // Останавливаем поллинг
                        return;
                    }

                    // 2. Запрос к блокчейну (синхронный вызов через RestTemplate)
                    BigDecimal currentBalance = walletService.getWalletBalance(walletAddress);

                    // 3. Проверка условий успешного пополнения
                    if (currentBalance.compareTo(targetAmount) >= 0) {
                        walletService.updateBalance(walletAddress,currentBalance);
                        userExchangeService.saveExchange(userId,walletAddress,phone,rubAmount,currentBalance);
                        futureHolder[0].cancel(false); // Останавливаем поллинг
                    }
                } catch (Exception e) {
                    // Логируем ошибку ноды, но не даем ей сломать планировщик
                    System.err.println("Ошибка при проверке баланса для " + walletAddress + ": " + e.getMessage());
                }
            }
        };
        // Запуск задачи каждые 3 секунды (3000 миллисекунд)
        futureHolder[0] = taskScheduler.scheduleAtFixedRate(pollingTask, Duration.ofSeconds(3));
    }
}
