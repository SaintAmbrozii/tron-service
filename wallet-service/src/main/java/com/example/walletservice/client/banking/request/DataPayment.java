package com.example.walletservice.client.banking.request;

import lombok.Data;

@Data
public class DataPayment {

    private String wallet;
    private Integer amount;
}
