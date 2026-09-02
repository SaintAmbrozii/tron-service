package com.example.walletservice.controller;

import com.example.walletservice.domain.Exchange;
import com.example.walletservice.dto.ExchangeDto;
import com.example.walletservice.dto.RequestExchangeToRub;
import com.example.walletservice.dto.ResponseExchangeToRub;
import com.example.walletservice.service.ExchangeService;
import com.example.walletservice.service.UserExchangeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseExchangeToRub usdToRub(@RequestBody RequestExchangeToRub exchange,
                                          @RequestHeader(name = USER_ID_HEADER_NAME) String userId) {
        log.info("Received POST request to exchange to rub. UserId {}, request {}",
                userId, exchange);
        return exchangeService.getUsd_exchange(userId,exchange);
    }

    @GetMapping
    public List<ExchangeDto> findByUserId(@RequestHeader(name = USER_ID_HEADER_NAME) String userId) {
        log.info("Received GET request to exchange to rub. UserId {}",
                userId);
        return userExchangeService.getUserExchages(userId);
    }
}
