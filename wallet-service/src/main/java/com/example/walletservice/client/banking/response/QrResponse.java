package com.example.walletservice.client.banking.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QrResponse {

    @JsonProperty("Data")
    private QrData data;

    @JsonProperty("Links")
    private Links links;

    @JsonProperty("Meta")
    private Meta meta;
}
