package com.example.walletservice.client.banking.payload.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentData {

    private String merchantId;
    private String legalId;
    private String customerCode;
    private Double amount;
    private String currency;
    private String paymentPurpose;
    private String qrcType;
    private ImageParams imageParams;
    private String sourceName;
}
