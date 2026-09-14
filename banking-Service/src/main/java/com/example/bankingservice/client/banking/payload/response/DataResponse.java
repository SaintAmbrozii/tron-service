package com.example.bankingservice.client.banking.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DataResponse {

    @JsonProperty("payload")
    private String payload;
    @JsonProperty("qrcId")
    private String qrcId;
    @JsonProperty("image")
    private Image image;
    @JsonProperty("Links")
    private Links links;
    @JsonProperty("Meta")
    private Meta meta;

}
