package com.example.walletservice.repo;

import com.example.walletservice.domain.Wallet;
import com.example.walletservice.dto.OperationExchange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepo extends JpaRepository<Wallet, UUID> {

    Wallet findByAddress (String address);

    Optional<Wallet> findByUserId (String userId);
}
