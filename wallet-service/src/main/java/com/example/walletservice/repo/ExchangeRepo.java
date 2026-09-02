package com.example.walletservice.repo;

import com.example.walletservice.domain.Exchange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExchangeRepo extends JpaRepository<Exchange, UUID> {

    Optional<Exchange> findById(UUID uuid);

    List<Exchange> findAllByUserId(String userid);
}
