package com.example.walletservice.service;

import com.example.walletservice.domain.Exchange;
import com.example.walletservice.domain.Outbox;
import com.example.walletservice.dto.ExchangeDto;
import com.example.walletservice.dto.OperationExchange;
import com.example.walletservice.event.BankingTransactionListener;
import com.example.walletservice.event.OutboxEvent;
import com.example.walletservice.exception.WalletServiceException;
import com.example.walletservice.repo.ExchangeRepo;
import com.example.walletservice.repo.OutboxRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserExchangeService {

    private final ExchangeRepo exchangeRepo;
    private final OutboxRepo outboxRepo;
    private final BankingTransactionListener bankingTransactionListener;
    private final SseService sseService;

    public UserExchangeService(ExchangeRepo exchangeRepo, OutboxRepo outboxRepo, BankingTransactionListener bankingTransactionListener, SseService sseService) {
        this.exchangeRepo = exchangeRepo;
        this.outboxRepo = outboxRepo;
        this.bankingTransactionListener = bankingTransactionListener;
        this.sseService = sseService;
    }

    @Transactional
    public void saveExchange(String userId, String address,String card,BigDecimal rubAmount,BigDecimal usdt) {

        Exchange exchange = Exchange.builder()
                .userId(userId)
                .userWallet(address)
                .rubAmount(rubAmount)
                .usdAmount(usdt)
                .status(false).build();
        Exchange saved = exchangeRepo.save(exchange);

        Outbox outbox = Outbox.builder()
                .amount(rubAmount)
                .userId(userId)
                .phone(card)
                .aggregateId(saved.getId())
                .status(false).build();

        Outbox indb = outboxRepo.save(outbox);

        bankingTransactionListener.onApplicationEvent(new OutboxEvent(indb.getId()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateExchande(UUID uuid) {
        Optional<Exchange> exchange = exchangeRepo.findById(uuid);
        if (exchange.isPresent()){
            Exchange updated = exchange.get();
            updated.setStatus(true);
            exchangeRepo.save(updated);
            OperationExchange response = OperationExchange.builder()
                    .message("Обмен успешно произведен").
                    usdt_amount(updated.getUsdAmount().doubleValue())
                    .rub_amount(updated.getRubAmount().doubleValue()).build();
            System.out.println("Обмен успешно произведен");
            sseService.sendNotification(updated.getUserId(), response);
        }
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
