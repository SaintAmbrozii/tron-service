package com.example.walletservice.controller;

import com.example.walletservice.service.ExchangeService;
import com.example.walletservice.service.UserExchangeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exchange/admin")
@Slf4j
public class WalletAdminController {

    private final ExchangeService exchangeService;
    private final UserExchangeService userExchangeService;

    public WalletAdminController(ExchangeService exchangeService, UserExchangeService userExchangeService) {
        this.exchangeService = exchangeService;
        this.userExchangeService = userExchangeService;
    }
}
