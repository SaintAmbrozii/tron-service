package com.example.bankingservice.controller;

import com.example.bankingservice.client.banking.payload.request.QrRequest;
import com.example.bankingservice.client.banking.payload.request.SpbData;
import com.example.bankingservice.client.banking.payload.response.DataResponse;
import com.example.bankingservice.client.banking.payload.response.QrResponse;
import com.example.bankingservice.service.BankingService;
import com.example.bankingservice.service.ListenerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentsController {

    public static final String USER_ID_HEADER_NAME = "X-USER-ID";
    public static final String AGGREGATE_ID = "X-AGGREGATE";

    private final ListenerService paymentsService;
    private final BankingService bankingService;

    public PaymentsController(ListenerService paymentsService, BankingService bankingService) {
        this.paymentsService = paymentsService;
        this.bankingService = bankingService;
    }

    @PostMapping("toSbp")
    public QrResponse dataResponse(@RequestBody QrRequest qrRequest, @RequestHeader(name = USER_ID_HEADER_NAME) String userId,
                                   @RequestHeader(name = AGGREGATE_ID) String aggregateId){
        return bankingService.getQr(qrRequest,userId,aggregateId);
    }


}
