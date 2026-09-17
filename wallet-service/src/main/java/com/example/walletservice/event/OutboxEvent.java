package com.example.walletservice.event;

import java.util.UUID;

public record OutboxEvent(UUID outboxId) {
}
