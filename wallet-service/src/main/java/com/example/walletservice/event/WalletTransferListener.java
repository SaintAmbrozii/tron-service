package com.example.walletservice.event;

import com.example.walletservice.domain.Wallet;
import com.example.walletservice.service.BlockChainWalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Optional;

@Slf4j
@Component
public class WalletTransferListener {

    private final BlockChainWalletService blockChainWalletService;

    public WalletTransferListener(BlockChainWalletService blockChainWalletService) {
        this.blockChainWalletService = blockChainWalletService;
    }


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApplicationEvent(TransferEvent event) {

        log.info("[СЛУШАТЕЛЬ] Транзакция БД закоммичена. Запуск асинхронного пайплайна для {}", event.address());

        blockChainWalletService.transferUsdToAdminWalletAsync(event.privatKey(), event.address(), event.amount())
                .thenAccept(finalUsdtTxId -> {

                    log.info("[УСПЕХ ЭВАКУАЦИИ] Средства успешно переведены на админ-кошелек. Wallet: {}, Amount: {}, TxID: {}",
                            event.address(), event.amount(), finalUsdtTxId);

                })
                .exceptionally(e -> {

                    Throwable cause = e.getCause() != null ? e.getCause() : e;

                    log.error("[КРИТИЧЕСКАЯ ОШИБКА ЭВАКУАЦИИ] Пайплайн завершился сбоем для кошелька {}. Сумма: {}. Причина: {}",
                            event.address(), event.amount(), cause.getMessage());

                    return null;
                });

        log.info("[СЛУШАТЕЛЬ] Пайплайн успешно делегирован в фон для кошелька {}. Поток свободен.", event.address());
    }

}
