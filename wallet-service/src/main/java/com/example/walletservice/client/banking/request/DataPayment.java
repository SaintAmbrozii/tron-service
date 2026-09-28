package com.example.walletservice.client.banking.request;

import com.example.walletservice.utils.TronAddress;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DataPayment {


    @TronAddress
    private String wallet;

    @Min(1000)
    private Integer amount;
}
