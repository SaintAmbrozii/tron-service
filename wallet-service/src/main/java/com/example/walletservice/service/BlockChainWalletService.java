package com.example.walletservice.service;

import com.example.walletservice.domain.Exchange;
import com.example.walletservice.domain.Status;
import com.example.walletservice.repo.ExchangeRepo;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
public class BlockChainWalletService {

    private static final String owner_private_key = "4a6213c5c05dd3dc8793837dde88b74bd9a45b1b9dc7663f21b771bdd56ad50b";
    private static final String usdtContractAddres = "TXYZopYRdj2D9XRtbG411XZZ3kM5VkAeBf";

    private static final String owner_address = "TUoHaVjx7n5xz8LwPRDckgFrDWhMhuSuJM";

    private static final String OWNER_ADDRESS = new KeyPair(owner_private_key).toBase58CheckAddress();

    private final ApiWrapper apiWrapper;
    private final ExchangeRepo exchangeRepo;

    public BlockChainWalletService(ApiWrapper apiWrapper, ExchangeRepo exchangeRepo) {
        this.apiWrapper = apiWrapper;
        this.exchangeRepo = exchangeRepo;
    }

    public void transferUsdToAdminWallet(String childPrivateKey, String childAddress, BigDecimal usdtAmount)
            throws Exception {
        log.info("[ПАЙПЛАЙН] Начало эвакуации {} USDT с кошелька {}", usdtAmount, childAddress);

        long trxAmountSun = 80_000_000L; // 80 TRX

        // --- ЭТАП 1: Пополнение дочернего кошелька TRX для оплаты fee ---
        Response.TransactionExtention trxTxExt = apiWrapper.transfer(OWNER_ADDRESS, childAddress, trxAmountSun);
        Chain.Transaction signedTrxTx = apiWrapper.signTransaction(trxTxExt);
        String trxTxId = apiWrapper.broadcastTransaction(signedTrxTx);

        if (trxTxId == null || trxTxId.isEmpty()) {
            throw new RuntimeException("Блокчейн отклонил транзакцию пополнения TRX");
        }
        log.info("[ЭТАП 1] TRX отправлен. TxID: {}. Ожидание подтверждения...", trxTxId);

        if (!waitForConfirmation(apiWrapper, trxTxId)) {
            throw new RuntimeException("TRX-пополнение не подтвердилось. Отмена.");
        }
        log.info("[ЭТАП 1] TRX подтверждён.");

        // --- ЭТАП 2: Перевод USDT с дочернего на родительский ---
        BigInteger rawUsdtAmount = usdtAmount.multiply(new BigDecimal(1_000_000)).toBigInteger();

        ApiWrapper childApiWrapper = null;
        try {
            childApiWrapper = ApiWrapper.ofNile(childPrivateKey);
            Contract smartContract = childApiWrapper.getContract(usdtContractAddres);
            Trc20Contract usdtToken = new Trc20Contract(smartContract, childAddress, childApiWrapper);

            long feeLimit = 100_000_000L;
            String usdtTxId = usdtToken.transfer(
                    OWNER_ADDRESS,
                    rawUsdtAmount.longValueExact(),
                    0,
                    "Sweep to Parent",
                    feeLimit
            );

            if (usdtTxId == null || usdtTxId.isEmpty()) {
                throw new RuntimeException("Пустой TX ID для USDT-перевода");
            }
            log.info("[ЭТАП 2] USDT отправлен. TxID: {}. Ожидание подтверждения...", usdtTxId);

            if (!waitForConfirmation(childApiWrapper, usdtTxId)) {
                throw new RuntimeException(
                        "USDT-перевод не подтвердился. TRX уже потрачены на fee. " +
                                "Требуется ручная проверка кошелька: " + childAddress
                );
            }

            log.info("[ПАЙПЛАЙН ЗАВЕРШЕН] USDT эвакуированы. TxID: {}", usdtTxId);
            System.out.println(usdtTxId);

        } finally {
            if (childApiWrapper != null) {
                childApiWrapper.close();
            }
        }
    }

    private boolean waitForConfirmation(ApiWrapper client, String txId) {
        int maxAttempts = 12; // 12 попыток * 3 секунды = 36 секунд максимум
        int delayMs = 3000;   // Время генерации блока в TRON в среднем 3 секунды

        for (int i = 0; i < maxAttempts; i++) {
            try {
                Thread.sleep(delayMs);
                Response.TransactionInfo info = client.getTransactionInfoById(txId);

                if (info != null) {
                    if (info.getResultValue() == 0) { // 0 означает SUCCESS в кодах TRON
                        return true;
                    } else {
                        log.error("Транзакция пополнения TRX завалилась на ноде с кодом ошибки: {}", info.getResultValue());
                        return false;
                    }
                }
            } catch (Exception e) {
                log.debug("Транзакция {} еще не попала в блок, ожидаем... (Попытка {})", txId, i + 1);
            }
        }
        return false;
    }


    public String transferUsdtToWallet(String targetAddress, BigDecimal amount) {
        log.info("[ПЕРЕВОД USDT] Отправка {} USDT на кошелек {}...", amount, targetAddress);

        // 1. Валидация входных данных
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма перевода должна быть больше нуля");
        }
        if (targetAddress == null || targetAddress.isEmpty()) {
            throw new IllegalArgumentException("Адрес получателя не может быть пустым");
        }

        BigInteger rawAmount = amount.multiply(new BigDecimal(1_000_000)).toBigInteger();

        try {

            Contract smartContract = apiWrapper.getContract(usdtContractAddres);
            Trc20Contract usdtToken = new Trc20Contract(smartContract, OWNER_ADDRESS, apiWrapper);

            long feeLimit = 100_000_000L; // 100 TRX

            // Отправка в сеть
            String txId = usdtToken.transfer(targetAddress, rawAmount.longValueExact(), 0, "External Transfer", feeLimit);

            if (txId == null || txId.isEmpty()) {
                throw new RuntimeException("Сеть TRON вернула пустой или невалидный TX ID.");
            }

            log.info("[УСПЕХ] Транзакция отправлена в сеть TRON. TxID: {}", txId);
            return txId;

        } catch (Exception e) {
            log.error("[ОШИБКА] Не удалось отправить транзакцию USDT на адрес {}", targetAddress, e);
            // Обязательно пробрасываем ошибку, чтобы вызывающий сервис знал о сбое!
            throw new RuntimeException("Ошибка выполнения транзакции в блокчейне", e);
        }
    }


}
