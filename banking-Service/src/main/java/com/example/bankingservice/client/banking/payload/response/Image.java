package com.example.bankingservice.client.banking.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Image {

    @JsonProperty("width")
    private int width;
    @JsonProperty("height")
    private int height;
    @JsonProperty("mediaType")
    private String mediaType;
    @JsonProperty("content")
    private String content; // b
}
