package com.example.walletservice.service;

import com.example.walletservice.domain.Exchange;
import com.example.walletservice.domain.Outbox;
import com.example.walletservice.domain.Status;
import com.example.walletservice.dto.ExchangeDto;
import com.example.walletservice.dto.OperationExchange;
import com.example.walletservice.event.BankingTransactionListener;
import com.example.walletservice.event.OutboxEvent;
import com.example.walletservice.exception.WalletServiceException;
import com.example.walletservice.repo.ExchangeRepo;
import com.example.walletservice.repo.OutboxRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserExchangeService {

    private final ExchangeRepo exchangeRepo;
    private final OutboxRepo outboxRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final SseService sseService;
    private final BlockChainWalletService blockChainWalletService;
    private final Executor asyncTaskExecutor;
    private final ExchangeStatusHolder statusHolder;

    public UserExchangeService(ExchangeRepo exchangeRepo, OutboxRepo outboxRepo,
                               ApplicationEventPublisher eventPublisher, SseService sseService,
                               BlockChainWalletService blockChainWalletService,  Executor asyncTaskExecutor,
                               ExchangeStatusHolder statusHolder) {
        this.exchangeRepo = exchangeRepo;
        this.outboxRepo = outboxRepo;
        this.eventPublisher = eventPublisher;
        this.sseService = sseService;
        this.blockChainWalletService = blockChainWalletService;
        this.asyncTaskExecutor = asyncTaskExecutor;
        this.statusHolder = statusHolder;
    }

    @Transactional
    public void saveExchange(String userId, String address,String card,BigDecimal rubAmount,BigDecimal usdt) {

        Exchange exchange = Exchange.builder()
                .userId(userId)
                .userWallet(address)
                .rubAmount(rubAmount)
                .usdAmount(usdt)
                .status(Status.CREATED).build();
        Exchange saved = exchangeRepo.save(exchange);

        Outbox outbox = Outbox.builder()
                .amount(rubAmount)
                .userId(userId)
                .phone(card)
                .aggregateId(saved.getId())
                .status(false).build();

        Outbox indb = outboxRepo.save(outbox);

        eventPublisher.publishEvent(new OutboxEvent(indb.getId()));
    }

    @Transactional
    public void updateExchande(UUID uuid) {
        Optional<Exchange> exchange = exchangeRepo.findById(uuid);
        if (exchange.isPresent()){
            Exchange updated = exchange.get();
            updated.setStatus(Status.COMPLETED);
            exchangeRepo.save(updated);
            OperationExchange response = OperationExchange.builder()
                    .message("Обмен успешно произведен").
                    usdt_amount(updated.getUsdAmount().doubleValue())
                    .rub_amount(updated.getRubAmount().doubleValue()).build();

            sseService.sendNotification(updated.getUserId(), response);
        }
    }

    public void getExchangeToTransfer(UUID uuid) {

        boolean started = statusHolder.lockAndMoveToProcessing(uuid);

        if (!started) {
            log.warn("Заявка {} пропущена: не найдена или уже обрабатывается/завершена", uuid);
            return;
        }

        CompletableFuture.runAsync(() -> {

            Exchange exchange = exchangeRepo.findById(uuid).orElse(null);
            if (exchange == null) return;

            try {

                String txId = blockChainWalletService.transferUsdtToWallet(exchange.getUserWallet(), exchange.getUsdAmount());

                statusHolder.moveToCompleted(uuid, txId);

                OperationExchange response = OperationExchange.builder()
                        .message("Обмен успешно произведен").
                        usdt_amount(exchange.getUsdAmount().doubleValue())
                        .rub_amount(exchange.getRubAmount().doubleValue()).build();

                sseService.sendNotification(exchange.getUserId(), response);

            } catch (Exception e) {

                log.error("Первичная отправка для exchange {} не удалась. Откатываем для повтора шедулером.", uuid, e);

                statusHolder.moveToRetryOrFailed(uuid, e.getMessage());
            }
        });
    }


    public List<ExchangeDto> getUserExchages(String userId){
        return exchangeRepo.findAllByUserId(userId).stream().map(ExchangeDto::toDto).collect(Collectors.toList());
    }

    public ExchangeDto findByUUID (UUID uuid){
        Optional<Exchange> exchange = exchangeRepo.findById(uuid);
        if (exchange.isPresent()) {
            Exchange inDB = exchange.get();
            return ExchangeDto.toDto(inDB);
        } else throw new WalletServiceException("нет данного id");
    }
}
