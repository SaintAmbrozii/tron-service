package com.example.walletservice.client.banking.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QrRequest {

    @JsonProperty("Data")
    SpbData data;
}
