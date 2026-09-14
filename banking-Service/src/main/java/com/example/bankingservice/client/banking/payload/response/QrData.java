package com.example.bankingservice.client.banking.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QrData {

    @JsonProperty("status")
    private String status;

    @JsonProperty("payload")
    private String payload;

    @JsonProperty("accountId")
    private String accountId;

    @JsonProperty("createdAt")
    private OffsetDateTime createdAt;

    @JsonProperty("merchantId")
    private String merchantId;

    @JsonProperty("legalId")
    private String legalId;

    @JsonProperty("qrcId")
    private String qrcId;

    @JsonProperty("amount")
    private Long amount;

    @JsonProperty("ttl")
    private String ttl;

    @JsonProperty("paymentPurpose")
    private String paymentPurpose;

    @JsonProperty("image")
    private Image image;

    @JsonProperty("commissionPercent")
    private Double commissionPercent;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("qrcType")
    private String qrcType;

    @JsonProperty("templateVersion")
    private String templateVersion;

    @JsonProperty("sourceName")
    private String sourceName;


}
