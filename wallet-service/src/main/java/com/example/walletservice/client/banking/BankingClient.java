package com.example.walletservice.client.banking;


import com.example.walletservice.client.banking.request.QrRequest;
import com.example.walletservice.client.banking.response.QrResponse;

public interface BankingClient {

    QrResponse getQrCode(QrRequest data,String userId,String aggregateId);
}
