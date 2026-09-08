package com.example.notificacionservice.client.wallet;

import com.example.notificacionservice.client.wallet.response.ExchangeDto;

import java.util.UUID;

public interface WalletClient {

    ExchangeDto getExchange(UUID id);
}
