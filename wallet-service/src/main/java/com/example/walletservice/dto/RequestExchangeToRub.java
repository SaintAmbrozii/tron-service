package com.example.walletservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestExchangeToRub {


    @NotBlank(message = "Номер не должен быть пустым")
    private String fromCardNumber;

    @NotBlank(message = "Сумма должна быть указана")
    @Min(10)
    private Double usd_amount;

}
