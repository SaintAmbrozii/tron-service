package com.example.bankingservice.client.banking.payload.request;

import lombok.*;
import org.checkerframework.checker.units.qual.N;

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
