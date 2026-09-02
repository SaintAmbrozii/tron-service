package com.example.notificacionservice.service;

import com.example.avro.outbox.PaymentDataEvent;
import com.example.notificacionservice.client.user.UserClientImpl;
import com.example.notificacionservice.client.user.response.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    private final UserClientImpl userClient;

    public NotificationService(UserClientImpl userClient) {
        this.userClient = userClient;
    }

    @Async("notificationExecutor")
    public void sendNotification(PaymentDataEvent event) {

        String userId = event.getUserId();

        System.out.println(userId);

        try {

            UserDto userDto = userClient.getUser(userId);

            System.out.println(userDto);

            log.info("Уведомление успешно отправлено {}", userId);

        } catch (Exception e) {

            log.error("Ошибка обработки уведомления для userId: " + userId, e);
        }


    }
}
