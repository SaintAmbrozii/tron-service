package com.example.walletservice.repo;

import com.example.walletservice.domain.Wallet;
import com.example.walletservice.dto.OperationExchange;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepo extends JpaRepository<Wallet, UUID> {

    Wallet findByAddress (String address);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.address = :address")
    Optional<Wallet> findByAddressToUpdate(@Param("address") String address);



    Optional<Wallet> findByUserId (String userId);
}
