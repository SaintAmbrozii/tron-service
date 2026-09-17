package com.example.walletservice.service;

import com.example.walletservice.domain.Status;
import com.example.walletservice.repo.ExchangeRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class ExchangeStatusHolder {

    private final ExchangeRepo exchangeRepo;

    public ExchangeStatusHolder(ExchangeRepo exchangeRepo) {
        this.exchangeRepo = exchangeRepo;
    }

    @Transactional
    public boolean lockAndMoveToProcessing(UUID uuid) {
        return exchangeRepo.findByIdForUpdate(uuid)
                .filter(ex -> ex.getStatus() == Status.CREATED || ex.getStatus() == Status.PENDING_RETRY)
                .map(ex -> {
                    ex.setStatus(Status.PROCESSING);
                    exchangeRepo.save(ex);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public void moveToCompleted(UUID uuid, String txId) {
        exchangeRepo.findById(uuid).ifPresent(ex -> {
            ex.setStatus(Status.COMPLETED);
            ex.setTxId(txId);
            ex.setFailReason(null);
            exchangeRepo.save(ex);
            log.info("[УСПЕХ] Заявка {} переведена в статус COMPLETED. TxID: {}", uuid, txId);
        });
    }

    @Transactional
    public void moveToRetryOrFailed(UUID uuid, String errorMessage) {
        exchangeRepo.findById(uuid).ifPresent(ex -> {
            int currentAttempts = ex.getRetryCount() + 1;
            ex.setRetryCount(currentAttempts);
            ex.setFailReason(errorMessage);

            if (currentAttempts >= 5) {
                ex.setStatus(Status.FAILED);
                log.error("[ФЕЙЛ] Заявка {} исчерпала лимит попыток и переведена в FAILED", uuid);
            } else {
                ex.setStatus(Status.PENDING_RETRY);
                log.info("[ПОВТОР] Заявка {} возвращена в очередь (RETRY). Попытка: {}", uuid, currentAttempts);
            }
            exchangeRepo.save(ex);
        });
    }


}
