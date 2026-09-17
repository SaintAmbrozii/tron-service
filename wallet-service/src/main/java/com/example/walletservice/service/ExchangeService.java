package com.example.walletservice.service;

import com.example.walletservice.client.banking.BankingClient;
import com.example.walletservice.client.banking.request.DataPayment;
import com.example.walletservice.client.banking.request.ImageParams;
import com.example.walletservice.client.banking.request.QrRequest;
import com.example.walletservice.client.banking.request.SpbData;
import com.example.walletservice.client.banking.response.QrResponse;
import com.example.walletservice.domain.Exchange;
import com.example.walletservice.domain.Status;
import com.example.walletservice.repo.ExchangeRepo;
import com.example.walletservice.tronclient.TronClient;
import com.example.walletservice.dto.OperationExchange;
import com.example.walletservice.dto.RequestExchangeToRub;
import com.example.walletservice.dto.ResponseExchangeToRub;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.contract.Contract;
import org.tron.trident.core.contract.Trc20Contract;

import java.math.BigDecimal;
import java.math.*;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class ExchangeService {

    private static final String owner_private_key = "4a6213c5c05dd3dc8793837dde88b74bd9a45b1b9dc7663f21b771bdd56ad50b";
    private static final String usdtContractAddress = "TXYZopYRdj2D9XRtbG411XZZ3kM5VkAeBf";
    private static final String accountId = "12345810901234567890/044525104";
    private static final String merchantId = "MF0000000001";

    private final WalletService walletService;
    private final ApiExchangeSerivce cbRfService;
    private final UserExchangeService userExchangeService;
    private final PollingService pollingService;
    private final SseService sseService;
    private final Map<String, Boolean> activePollings = new ConcurrentHashMap<>();
    private final AtomicInteger activePollingsGauge;
    private final BankingClient bankingClient;
    private final ExchangeRepo exchangeRepo;

    public ExchangeService(WalletService walletService, ApiExchangeSerivce cbRfService, UserExchangeService userExchangeService,
                           PollingService pollingService, SseService sseService,
                           MeterRegistry meterRegistry, BankingClient bankingClient, ExchangeRepo exchangeRepo) {
        this.walletService = walletService;
        this.cbRfService = cbRfService;
        this.userExchangeService = userExchangeService;
        this.pollingService = pollingService;
        this.sseService = sseService;
        this.activePollingsGauge = meterRegistry.gauge("trident_active_pollings", new AtomicInteger(0));
        this.bankingClient = bankingClient;
        this.exchangeRepo = exchangeRepo;
    }



    public ResponseExchangeToRub getUsd_exchange (String userId, RequestExchangeToRub requestExchange) {

        String address = walletService.generate(userId);

        BigDecimal rubAmount = BigDecimal.valueOf(cbRfService.convertUsdToRub(requestExchange.getUsd_amount()))
                .setScale(2,RoundingMode.HALF_UP);

        pollingService.startPolling(userId,address,requestExchange.getFromCardNumber(),rubAmount,BigDecimal.valueOf(requestExchange.getUsd_amount()));

        return ResponseExchangeToRub.builder().address(address).summa(rubAmount.doubleValue()).build();

    }

    public QrResponse getQRRubToUsd (DataPayment payment, String userId) {

        SpbData paymentData = SpbData.builder()
                .amount(payment.getAmount())
                .currency("RUB")
                .qrcType("02")
                .sourceName("string")
                .paymentPurpose("Bill")
                .customerCode(accountId)
                .merchantId(merchantId)
                .imageParams(ImageParams.builder().height(200).width(200).mediaType("image/png").build()).build();

        QrRequest request = QrRequest.builder().data(paymentData).build();

        double rubAmount = Double.valueOf(payment.getAmount())/100;

        BigDecimal usdAmount = BigDecimal.valueOf(cbRfService.convertRubToUsd(rubAmount))
                        .setScale(2,RoundingMode.HALF_UP);

        Exchange exchange = Exchange.builder()
                .userId(userId)
                .rubAmount(BigDecimal.valueOf(rubAmount))
                .usdAmount(usdAmount)
                .userWallet(payment.getWallet())
                .status(Status.CREATED).build();

        Exchange saved = exchangeRepo.save(exchange);

        QrResponse response = bankingClient.getQrCode(request, userId, saved.getId().toString());

        sseService.createEmitter(userId);

        return response;
    }


    private void handleSuccess(String address, String userId,String user_card, BigDecimal currentBalance, BigDecimal rubAmount) {
        try {
            userExchangeService.saveExchange(userId,address,user_card,rubAmount,currentBalance);
            walletService.updateBalance(address,currentBalance);
        } finally {
            activePollings.remove(address);
            activePollingsGauge.decrementAndGet();
        }
    }

    private void handleTimeout(String walletId, String userId, BigDecimal expectedUsdt, BigDecimal rubAmount) {
        try {
            OperationExchange response = OperationExchange.builder().message("Время ожидания (10 минут) пополнения кошелька " + walletId + " истекло.").
                    usdt_amount(expectedUsdt.doubleValue()).rub_amount(rubAmount.doubleValue()).build();
            sseService.sendNotification(userId, response);
            log.info("Время ожидания (10 минут) пополнения кошелька " + walletId + " истекло.");
        } finally {
            activePollings.remove(walletId);
            activePollingsGauge.decrementAndGet();
        }
    }

    public void clearActivePollings() {
        this.activePollings.clear();
    }



}
