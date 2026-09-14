package com.example.walletservice.event;

import com.example.walletservice.repo.WalletRepo;
import com.example.walletservice.service.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class WalletTransferListener implements ApplicationListener<TransferEvent> {

    private final WalletRepo walletRepo;
    private final WalletService walletService;

    public WalletTransferListener(WalletRepo walletRepo, WalletService walletService) {
        this.walletRepo = walletRepo;
        this.walletService = walletService;
    }

    @Override
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApplicationEvent(TransferEvent event) {

        transfer(event);

    }

    private void transfer(TransferEvent event) {

    }


}
