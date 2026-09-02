package com.example.bankingservice.controller;

import com.example.bankingservice.service.ListenerService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentsController {

    private final ListenerService paymentsService;

    public PaymentsController(ListenerService paymentsService) {
        this.paymentsService = paymentsService;
    }


}
