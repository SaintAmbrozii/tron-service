package com.example.walletservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseExchangeToRub {

    public String address;

    public Double summa;
}
