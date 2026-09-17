package com.example.walletservice.event;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferEvent(UUID walletId, String address, String privatKey, BigDecimal amount) {
}
