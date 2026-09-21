package com.example.walletservice.service;

import com.example.walletservice.domain.Exchange;
import com.example.walletservice.domain.Status;
import com.example.walletservice.repo.ExchangeRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.contract.Contract;
import org.tron.trident.core.contract.Trc20Contract;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Response;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class BlockChainWalletService {

    private static final String owner_private_key = "4a6213c5c05dd3dc8793837dde88b74bd9a45b1b9dc7663f21b771bdd56ad50b";
    private static final String usdtContractAddres = "TXYZopYRdj2D9XRtbG411XZZ3kM5VkAeBf";

    private static final String owner_address = "TUoHaVjx7n5xz8LwPRDckgFrDWhMhuSuJM";

    private static final String OWNER_ADDRESS = new KeyPair(owner_private_key).toBase58CheckAddress();

    private final ApiWrapper apiWrapper;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final ExchangeRepo exchangeRepo;

    public BlockChainWalletService(ApiWrapper apiWrapper, ExchangeRepo exchangeRepo) {
        this.apiWrapper = apiWrapper;
        this.exchangeRepo = exchangeRepo;
    }


    @Async("blockchainTaskExecutor")
    public CompletableFuture<String> transferUsdToAdminWalletAsync(String childPrivateKey, String childAddress, BigDecimal usdtAmount) {
        log.info("[ПАЙПЛАЙН] [ЭТАП 1] Поток {} начинает пополнение TRX для {}", Thread.currentThread().getName(), childAddress);

        if (usdtAmount == null || usdtAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма эвакуации должна быть больше нуля");
        }

        try {
            long trxAmountSun = 80_000_000L; // 80 TRX

            Response.TransactionExtention trxTxExt = apiWrapper.transfer(OWNER_ADDRESS, childAddress, trxAmountSun);
            Chain.Transaction signedTrxTx = apiWrapper.signTransaction(trxTxExt);
            String trxTxId = apiWrapper.broadcastTransaction(signedTrxTx);

            if (trxTxId == null || trxTxId.isEmpty()) {
                throw new RuntimeException("Блокчейн отклонил транзакцию пополнения TRX");
            }
            log.info("[ЭТАП 1] TRX отправлен. TxID: {}. Ожидание подтверждения...", trxTxId);

            return waitForConfirmationAsync(apiWrapper, trxTxId, 1, 12)
                    .thenCompose(confirmedTrxId -> {
                        return executeUsdtSweepAsync(childPrivateKey, childAddress, usdtAmount);
                    });

        } catch (Exception e) {
            log.error("[ЭТАП 1 ОШИБКА] Сбой при инициализации пополнения TRX для {}", childAddress, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async("blockchainTaskExecutor")
    public CompletableFuture<String> executeUsdtSweepAsync(String childPrivateKey, String childAddress, BigDecimal usdtAmount) {
        log.info("[ПАЙПЛАЙН] [ЭТАП 2] Поток {} начинает вывод {} USDT на родительский кошелек", Thread.currentThread().getName(), usdtAmount);

        CompletableFuture<String> resultFuture = new CompletableFuture<>();
        BigInteger rawUsdtAmount = usdtAmount.multiply(new BigDecimal(1_000_000)).toBigInteger();

        ApiWrapper childApiWrapper = null;
        try {
            childApiWrapper = ApiWrapper.ofNile(childPrivateKey);
            Contract smartContract = childApiWrapper.getContract(usdtContractAddres);
            Trc20Contract usdtToken = new Trc20Contract(smartContract, childAddress, childApiWrapper);

            long feeLimit = 100_000_000L;
            String usdtTxId = usdtToken.transfer(OWNER_ADDRESS, rawUsdtAmount.longValueExact(), 0, "Sweep to Parent", feeLimit);

            if (usdtTxId == null || usdtTxId.isEmpty()) {
                throw new RuntimeException("Пустой TX ID для USDT-перевода");
            }
            log.info("[ЭТАП 2] USDT отправлен. TxID: {}. Ожидание финального подтверждения...", usdtTxId);

            final ApiWrapper finalWrapper = childApiWrapper;

            waitForConfirmationAsync(finalWrapper, usdtTxId, 1, 12)
                    .thenAccept(resultFuture::complete)
                    .exceptionally(ex -> {
                        resultFuture.completeExceptionally(ex);
                        return null;
                    })
                    .whenComplete((res, ex) -> {
                        log.info("[РЕСУРС] Закрытие асинхронного childApiWrapper для {}", childAddress);
                        finalWrapper.close();
                    });

        } catch (Exception e) {
            if (childApiWrapper != null) {
                childApiWrapper.close();
            }
            resultFuture.completeExceptionally(new RuntimeException("Ошибка на этапе отправки USDT", e));
        }

        return resultFuture;
    }

    private CompletableFuture<String> waitForConfirmationAsync(ApiWrapper client, String txId, int attempt, int maxAttempts) {
        if (attempt > maxAttempts) {
            return CompletableFuture.failedFuture(new RuntimeException("Таймаут ожидания подтверждения транзакции: " + txId));
        }

        CompletableFuture<String> future = new CompletableFuture<>();

        scheduler.schedule(() -> {
            try {
                Response.TransactionInfo info = client.getTransactionInfoById(txId);

                if (info != null) {
                    if (info.getResultValue() == 0) { // 0 = SUCCESS
                        future.complete(txId);
                    } else {
                        future.completeExceptionally(new RuntimeException("Транзакция " + txId + " отклонена нодой с кодом " + info.getResultValue()));
                    }
                } else {
                    retryConfirmation(client, txId, attempt, maxAttempts, future);
                }
            } catch (Exception e) {
                retryConfirmation(client, txId, attempt, maxAttempts, future);
            }
        }, 3, TimeUnit.SECONDS); // Опрос каждые 3 секунды

        return future;
    }

    private void retryConfirmation(ApiWrapper client, String txId, int attempt, int maxAttempts, CompletableFuture<String> originalFuture) {
        waitForConfirmationAsync(client, txId, attempt + 1, maxAttempts)
                .thenAccept(originalFuture::complete)
                .exceptionally(ex -> {
                    originalFuture.completeExceptionally(ex);
                    return null;
                });
    }


    @Async("asyncTaskExecutor") //
    public CompletableFuture<String> transferUsdtToWalletAsync(String targetAddress, BigDecimal amount) {
        log.info("[ПЕРЕВОД USDT] Поток {} начинает асинхронную отправку...", Thread.currentThread().getName());

        // Валидация
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            // ВАЖНО: При @Async исключения внутри метода автоматически упакуются в CompletableFuture
            throw new IllegalArgumentException("Сумма перевода должна быть больше нуля");
        }

        BigInteger rawAmount = amount.multiply(new BigDecimal(1_000_000)).toBigInteger();

        try {
            Contract smartContract = apiWrapper.getContract(usdtContractAddres);
            Trc20Contract usdtToken = new Trc20Contract(smartContract, OWNER_ADDRESS, apiWrapper);

            String txId = usdtToken.transfer(targetAddress, rawAmount.longValueExact(), 0, "External Transfer", 100_000_000L);

            if (txId == null || txId.isEmpty()) {
                throw new RuntimeException("Сеть TRON вернула пустой или невалидный TX ID.");
            }

            log.info("[УСПЕХ] TxID: {}", txId);

            // Оборачиваем результат в завершенную таску, Spring сам состыкует ее с асинхронным контекстом
            return CompletableFuture.completedFuture(txId);

        } catch (Exception e) {
            log.error("[ОШИБКА] Не удалось отправить транзакцию USDT на адрес {}", targetAddress, e);
            throw new RuntimeException("Ошибка выполнения транзакции в блокчейне", e);
        }
    }

}
