package com.example.bankingservice.service;

import com.example.avro.outbox.OutboxDataEvent;
import com.example.bankingservice.client.banking.payload.request.QrRequest;
import com.example.bankingservice.client.banking.payload.response.DataResponse;
import com.example.bankingservice.client.banking.payload.response.QrData;
import com.example.bankingservice.client.banking.payload.response.QrResponse;
import com.example.bankingservice.domain.Payments;
import com.example.bankingservice.repo.PaymentsRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class BankingService {

    private final PaymentsRepo paymentsRepo;
    private final RestClient restClient;
    private static final String accountId = "12345810901234567890/044525104";
    private static final String merchantId = "MF0000000001";
    private final BankingPollingService bankingPollingService;

    public BankingService(PaymentsRepo paymentsRepo, RestClient restClient, BankingPollingService bankingPollingService) {
        this.paymentsRepo = paymentsRepo;

        this.restClient = restClient;
        this.bankingPollingService = bankingPollingService;
    }



    public QrResponse getQr(QrRequest qrRequest,String userId,String aggregateId){

        DataResponse response = restClient.post()
                .uri("https://enter.tochka.com/sandbox/v2/sbp/v1.0/qr-code/merchant/{merchantId}/{accountId}",merchantId,accountId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(qrRequest)
                .retrieve()
                .body(DataResponse.class);

        if (response == null || response.getLinks() == null || response.getLinks().getSelf() == null) {
            throw new RuntimeException("Пустой ответ или нет self-ссылки");
        }

        Payments payments = Payments.builder()
                .userId(userId)
                .aggregateId(UUID.fromString(aggregateId))
                .phone("+79134896593")
                .amount(BigDecimal.valueOf(qrRequest.getData().getAmount()/100))
                .status(false).build();

        paymentsRepo.saveAndFlush(payments);
        String qrcId = "AD10001B38T99KA99CD8S37FMTH8CFQ7";

        bankingPollingService.startPolling(qrcId,UUID.fromString(aggregateId));
        QrResponse qrResponse = getQrCode(qrcId);

        return qrResponse;

    }

    public String getStatus(String urcId) {

        QrResponse response = restClient.get()
                .uri("https://enter.tochka.com/sandbox/v2/sbp/v1.0/qr-code/{qrcId}",urcId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                        .body(QrResponse.class);

        System.out.println("Status: " + response.getData().getStatus());
        System.out.println("QrcId: " + response.getData().getQrcId());

        return response.getData().getStatus();

    }

    public QrResponse getQrCode(String urcId) {

        QrResponse response = restClient.get()
                        .uri("https://enter.tochka.com/sandbox/v2/sbp/v1.0/qr-code/{qrcId}",urcId)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .body(QrResponse.class);

        System.out.println("Status: " + response.getData().getStatus());
        System.out.println("QrcId: " + response.getData().getQrcId());

        return response;

    }

}
