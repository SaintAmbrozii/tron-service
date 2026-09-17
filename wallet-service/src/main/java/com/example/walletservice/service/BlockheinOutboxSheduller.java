package com.example.walletservice.service;

import com.example.walletservice.domain.Exchange;
import com.example.walletservice.domain.Status;
import com.example.walletservice.repo.ExchangeRepo;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class BlockheinOutboxSheduller {

    private final ExchangeRepo exchangeRepo;
    private final UserExchangeService userExchangeService;

    public BlockheinOutboxSheduller(ExchangeRepo exchangeRepo, UserExchangeService userExchangeService) {
        this.exchangeRepo = exchangeRepo;
        this.userExchangeService = userExchangeService;
    }

    @Scheduled(fixedDelayString = "${app.outbox.scheduler-delay-ms:30000}") // Каждые 30 секунд
    @SchedulerLock(
            name = "ExchangeOutboxScheduler_retryFailedExchanges",
            lockAtMostFor = "5m",
            lockAtLeastFor = "10s"
    )
    public void retryFailedExchanges() {

        List<Exchange> retryQueue = exchangeRepo.findAllByStatus(Status.PENDING_RETRY);

        if (retryQueue.isEmpty()) {
            log.debug("[OUTBOX-ШЕДУЛЕР] Нет заявок для повторной отправки.");
            return;
        }

        log.info("[OUTBOX-ШЕДУЛЕР] Найдено {} заявок для обработки.", retryQueue.size());

        for (Exchange exchange : retryQueue) {
            try {
                log.info("[OUTBOX-ШЕДУЛЕР] Отправка заявки {} на повторный перевод.", exchange.getId());

                userExchangeService.getExchangeToTransfer(exchange.getId());

            } catch (Exception e) {

                log.error("[OUTBOX-ШЕДУЛЕР] Критическая ошибка при попытке запустить повтор для заявки {}", exchange.getId(), e);
            }
        }

        log.info("[OUTBOX-ШЕДУЛЕР] Сканирование очереди успешно завершено.");
    }
}
