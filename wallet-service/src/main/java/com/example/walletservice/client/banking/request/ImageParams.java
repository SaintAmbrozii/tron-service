package com.example.walletservice.client.banking.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImageParams {

    private Integer width;
    private Integer height;
    private String mediaType;
}
