package com.example.walletservice.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
@Setter
public class OutboxEvent extends ApplicationEvent {

    private UUID outboxId;

    public OutboxEvent(UUID outboxId) {
        super(outboxId);
        this.outboxId=outboxId;
    }
}
