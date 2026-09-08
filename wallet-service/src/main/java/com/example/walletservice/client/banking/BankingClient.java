package com.example.walletservice.client.banking;

import com.example.walletservice.client.banking.payload.request.PaymentData;
import com.example.walletservice.client.banking.payload.response.DataResponse;

public interface BankingClient {

    DataResponse getQrCode(PaymentData data);
}
