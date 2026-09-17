package com.example.walletservice.repo;

import com.example.walletservice.domain.Exchange;
import com.example.walletservice.domain.Status;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExchangeRepo extends JpaRepository<Exchange, UUID> {

    Optional<Exchange> findById(UUID uuid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Exchange e WHERE e.id = :id")
    Optional<Exchange> findByIdForUpdate(@Param("id") UUID id);

    List<Exchange> findAllByStatus(Status status);


    List<Exchange> findAllByUserId(String userid);
}
