package com.example.walletservice.service;

import com.example.walletservice.tronclient.TronClient;
import com.example.walletservice.domain.Wallet;
import com.example.walletservice.repo.WalletRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.contract.Contract;
import org.tron.trident.core.contract.Trc20Contract;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Response;

import java.math.BigDecimal;
import java.math.BigInteger;

@Slf4j
@Service
public class WalletService {

    private static final String owner_private_key = "4a6213c5c05dd3dc8793837dde88b74bd9a45b1b9dc7663f21b771bdd56ad50b";
    private static final String usdtContractAddres = "TXYZopYRdj2D9XRtbG411XZZ3kM5VkAeBf";

    private static final String owner_address = "TUoHaVjx7n5xz8LwPRDckgFrDWhMhuSuJM";

    private static final String OWNER_ADDRESS = new KeyPair(owner_private_key).toBase58CheckAddress();

    private final WalletRepo walletRepo;
    private final TronClient tronClient;
    private final ApiWrapper apiWrapper;

    public WalletService(WalletRepo walletRepo, TronClient tronClient, ApiWrapper apiWrapper) {
        this.walletRepo = walletRepo;
        this.tronClient = tronClient;
        this.apiWrapper = apiWrapper;
    }

    public String generate(String userId) {

    //    Optional<Wallet> userWallet = walletRepo.findByUserId(userId);

    //    if (userWallet.isPresent()) {
    //        return userWallet.get().getAddress();
    //    }
      //  ApiWrapper client = ApiWrapper.ofNile(owner_private_key);
        // 2. Generate a new key pair offline
        KeyPair newAccount = KeyPair.generate();
        String newAddress = newAccount.toBase58CheckAddress();
        String privatKey = newAccount.toPrivateKey();
        System.out.println(newAddress);
        Response.TransactionExtention transaction = null;
        try {
            transaction = apiWrapper.createAccount(OWNER_ADDRESS, newAddress);
        } catch (IllegalException e) {
            e.printStackTrace();
        }
// Подписываем транзакцию
        Chain.Transaction signedTxn = apiWrapper.signTransaction(transaction);
// Отправляем транзакцию в сеть
        String ret = apiWrapper.broadcastTransaction(signedTxn);
        System.out.println("Результат: " + ret);
        log.info("подпись" + ret);
        if (!ret.isEmpty()){
            Wallet wallet = Wallet.builder()
                    .userId(userId)
                    .address(newAddress)
                    .amount(BigDecimal.valueOf(0.00))
                    .privatKey(privatKey).build();
            walletRepo.save(wallet);
        }

        log.info("адрес" +newAddress);
        return newAddress;

    }

    public BigDecimal getWalletBalance(String address) {

        Contract tokenContract = apiWrapper.getContract(usdtContractAddres);
        // 3. Используем обертку Trc20Contract для удобной работы с токеном
        Trc20Contract usdtToken = new Trc20Contract(tokenContract, usdtContractAddres, apiWrapper);
        // 4. Запрашиваем баланс (возвращается в минимальных неделимых единицах)
        BigInteger rawBalance = usdtToken.balanceOf(address);
        // 5. Конвертируем в привычный формат (у USDT 6 знаков после запятой)
        BigDecimal usdtBalance = new BigDecimal(rawBalance).divide(new BigDecimal("1000000"));

        return usdtBalance;
    }

    @Transactional
    public void updateBalance(String walletAddress, BigDecimal balance) {
        Wallet inDB = walletRepo.findByAddress(walletAddress);
        inDB.setAmount(balance);
        walletRepo.save(inDB);
    }

    public Long getAccountBalance(String newAddress) {
        return apiWrapper.getAccountBalance(newAddress);
    }
}
