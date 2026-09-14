package com.example.walletservice.client.banking.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SpbData {

    private String merchantId;
    private String legalId;
    private String customerCode;
    private Integer amount;
    private String currency;
    private String paymentPurpose;
    private String qrcType;
    private ImageParams imageParams;
    private String sourceName;
}
