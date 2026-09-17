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

    @Async("blockchainTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApplicationEvent(TransferEvent event) {

        try {
            blockChainWalletService.transferUsdToAdminWallet(event.privatKey(),event.address(),event.amount());
            log.info("Transfer completed for wallet {}, amount {}",
                    event.address(), event.amount());
        } catch (Exception e) {
            log.error("Transfer failed for wallet {}, amount {}",
                    event.address(), event.amount(), e);
        }

    }

}
