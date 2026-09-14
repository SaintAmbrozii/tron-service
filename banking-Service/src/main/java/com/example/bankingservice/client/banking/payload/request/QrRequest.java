package com.example.bankingservice.client.banking.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class QrRequest {

    @JsonProperty("Data")
    SpbData data;
}
