package com.example.walletservice.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
@Setter
public class TransferEvent extends ApplicationEvent {

    private UUID walletId;

    public TransferEvent(UUID walletId) {
        super(walletId);
        this.walletId=walletId;
    }
}
