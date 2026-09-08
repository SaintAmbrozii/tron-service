package com.example.notificacionservice.client.wallet.response;


import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
public class ExchangeDto {

    private String userId;
    private String userWallet;
    private BigDecimal rubAmount;
    private BigDecimal usdAmount;
    private Boolean status;
    private ZonedDateTime createDate;


}
