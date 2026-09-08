package com.example.walletservice.client.banking.payload.response;

import lombok.Data;

@Data
public class Image {

    private Integer width;
    private Integer height;
    private String mediaType;
    private String content;
}
