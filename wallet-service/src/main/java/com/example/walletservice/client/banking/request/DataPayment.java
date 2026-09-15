package com.example.walletservice.client.banking.request;

import com.example.walletservice.utils.TronAddress;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DataPayment {

    @NotBlank(message = "Адрес не должен быть пустым")
    @TronAddress
    private String wallet;
    @NotBlank(message = "Сумма не должна быть меньше")
    @Min(1000)
    private Integer amount;
}
