package com.example.walletservice.dto;

import com.example.walletservice.domain.Exchange;
import jakarta.persistence.Column;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

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


    public static ExchangeDto toDto(Exchange exchange) {

        ExchangeDto dto = new ExchangeDto();
        dto.setCreateDate(exchange.getCreateDate());
        dto.setRubAmount(exchange.getRubAmount());
        dto.setUsdAmount(exchange.getUsdAmount());
        dto.setUserId(exchange.getUserId());
        return dto;
    }
}
