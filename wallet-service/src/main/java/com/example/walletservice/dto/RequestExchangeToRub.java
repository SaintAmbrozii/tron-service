package com.example.walletservice.dto;

import lombok.Data;

@Data
public class RequestExchangeToRub {


    private String fromCardNumber;

    private Double usd_amount;

}
