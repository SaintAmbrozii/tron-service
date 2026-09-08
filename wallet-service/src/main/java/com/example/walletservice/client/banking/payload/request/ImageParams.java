package com.example.walletservice.client.banking.payload.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageParams {

    private Integer width;
    private Integer height;
    private String mediaType;
}
