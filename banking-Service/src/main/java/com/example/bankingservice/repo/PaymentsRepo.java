package com.example.bankingservice.repo;

import com.example.bankingservice.domain.Payments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentsRepo extends JpaRepository<Payments, UUID> {
}
