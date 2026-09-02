package com.example.walletservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OperationExchange {

    private String message;

    private Double rub_amount;

    private Double usdt_amount;
}
