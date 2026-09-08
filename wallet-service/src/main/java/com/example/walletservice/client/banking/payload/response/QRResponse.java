package com.example.walletservice.client.banking.payload.response;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class QRResponse {

    private final String status;
    private final String payload;
    private final String accountId;
    private final OffsetDateTime createdAt; // Для формата "2019-01-01T06:06:06.364+00:00"
    private final String merchantId;
    private final String legalId;
    private final String qrcId;
    private final Long amount; // Для денежных сумм
    private final String ttl;
    private final String paymentPurpose;
    private final Image image; // Вложенный DTO картинки
    private final Double commissionPercent;
    private final String currency;
    private final String qrcType;
    private final String templateVersion;
    private final String sourceName;


}
