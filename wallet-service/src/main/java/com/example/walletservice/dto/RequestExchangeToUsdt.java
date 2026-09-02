package com.example.walletservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestExchangeToUsdt {

    private Double amount;

    private String wallet_address;
}
