package com.example.walletservice.service;

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

    private final WalletService walletService;
    private final ApiExchangeSerivce cbRfService;
    private final UserExchangeService userExchangeService;
    private final PollingService pollingService;
    private final SseService sseService;
    private final Map<String, Boolean> activePollings = new ConcurrentHashMap<>();
    private static final int MAX_SIMULTANEOUS_POLLINGS = 100;
    private final AtomicInteger activePollingsGauge;

    public ExchangeService(WalletService walletService, TronClient tronClient, ApiExchangeSerivce cbRfService, UserExchangeService userExchangeService,
                           PollingService pollingService, SseService sseService,
                           MeterRegistry meterRegistry) {
        this.walletService = walletService;
        this.cbRfService = cbRfService;
        this.userExchangeService = userExchangeService;
        this.pollingService = pollingService;
        this.sseService = sseService;
        this.activePollingsGauge = meterRegistry.gauge("trident_active_pollings", new AtomicInteger(0));
    }



    public ResponseExchangeToRub getUsd_exchange (String userId, RequestExchangeToRub requestExchange) {

        String address = walletService.generate(userId);

        BigDecimal rubAmount = BigDecimal.valueOf(cbRfService.convertUsdToRub(requestExchange.getUsd_amount()))
                .setScale(2,RoundingMode.HALF_UP);

        pollingService.startPolling(userId,address,requestExchange.getFromCardNumber(),rubAmount,BigDecimal.valueOf(requestExchange.getUsd_amount()));

        return ResponseExchangeToRub.builder().address(address).summa(rubAmount.doubleValue()).build();

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

    private BigDecimal getWalletBalance(String address) {

        ApiWrapper client = ApiWrapper.ofNile(owner_private_key);
        // 3. Адрес вашего кошелька
        String userWalletAddress = address;
        // 4. Подготовка вызова функции balanceOf в смарт-контракте
        Contract tokenContract = client.getContract(usdtContractAddress);
        // 3. Используем обертку Trc20Contract для удобной работы с токеном
        Trc20Contract usdtToken = new Trc20Contract(tokenContract, usdtContractAddress, client);
        // 4. Запрашиваем баланс (возвращается в минимальных неделимых единицах)
        BigInteger rawBalance = usdtToken.balanceOf(userWalletAddress);
        // 5. Конвертируем в привычный формат (у USDT 6 знаков после запятой)
        BigDecimal usdtBalance = new BigDecimal(rawBalance).divide(new BigDecimal("1000000"));

        try {
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return usdtBalance;
    }


}
