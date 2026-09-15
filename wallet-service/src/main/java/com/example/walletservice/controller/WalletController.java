package com.example.walletservice.controller;


import com.example.walletservice.client.banking.request.DataPayment;
import com.example.walletservice.client.banking.response.QrResponse;
import com.example.walletservice.domain.Exchange;
import com.example.walletservice.dto.ExchangeDto;
import com.example.walletservice.dto.RequestExchangeToRub;
import com.example.walletservice.dto.ResponseExchangeToRub;
import com.example.walletservice.service.ExchangeService;
import com.example.walletservice.service.UserExchangeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/exchange")
public class WalletController {

    public static final String USER_ID_HEADER_NAME = "X-USER-ID";

    private final ExchangeService exchangeService;
    private final UserExchangeService userExchangeService;

    public WalletController(ExchangeService exchangeService, UserExchangeService userExchangeService) {
        this.exchangeService = exchangeService;
        this.userExchangeService = userExchangeService;
    }

    @PostMapping("toRub")
    public ResponseExchangeToRub usdToRub(@RequestBody @Valid RequestExchangeToRub exchange,
                                          @RequestHeader(name = USER_ID_HEADER_NAME) String userId) {
        log.info("Received POST request to exchange to rub. UserId {}, request {}",
                userId, exchange);
        return exchangeService.getUsd_exchange(userId,exchange);
    }

    @PostMapping("toUsd")
    public QrResponse getQtPay(@RequestBody @Valid DataPayment payment, @RequestHeader(name = USER_ID_HEADER_NAME) String userId) {
        log.info("Received POST request to exchange to usd. UserId {}, request {}",
                userId, payment);
        return exchangeService.getQRRubToUsd(payment, userId);
    }

    @GetMapping
    public List<ExchangeDto> findByUserId(@RequestHeader(name = USER_ID_HEADER_NAME) String userId) {
        log.info("Received GET request to exchanges to rub. UserId {}",
                userId);
        return userExchangeService.getUserExchages(userId);
    }

    @GetMapping("{id}")
    public ExchangeDto findById(@RequestParam(name = "id")UUID id) {
        log.info("Received GET request to exchanges to rub. UserId {}", id);
        return userExchangeService.findByUUID(id);

    }


}
