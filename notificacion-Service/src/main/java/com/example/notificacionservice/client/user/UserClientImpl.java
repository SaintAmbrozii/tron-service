package com.example.notificacionservice.client.user;

import com.example.notificacionservice.client.UserClient;
import com.example.notificacionservice.client.user.response.UserDto;
import com.example.notificacionservice.properties.UserClientProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;

import static com.example.notificacionservice.utils.MaskingUtil.maskIfNeeded;


@Slf4j
@Component
public class UserClientImpl extends AbstractClient implements UserClient {

    private static final String USER_BACKEND = "userBackend";
    private static final String SERVICE_NAME = "user-service";
    private final UserClientProperties properties;
    private final RestClient userRestClient;

    public UserClientImpl(@Autowired(required = false) ObjectMapper objectMapper, UserClientProperties properties, RestClient userRestClient) {
        super(objectMapper);
        this.properties = properties;
        this.userRestClient = userRestClient;
    }

    @CircuitBreaker(name = USER_BACKEND)
    @Retry(name = USER_BACKEND)
    @Override
    public UserDto getUser(String userId) {
        UserDto body = userRestClient.get()
                .uri(properties.getNearestPath())
                .header("X-USER-ID",userId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(org.springframework.http.HttpStatusCode::isError,
                        (org.springframework.http.HttpRequest request, org.springframework.http.client.ClientHttpResponse response) -> {
                    org.springframework.http.HttpStatusCode status = response.getStatusCode();
                    if (status.is5xxServerError()) {
                        throw new com.example.notificacionservice.exception.RestClientRetryableException(
                                "%s returned %s status code".formatted(SERVICE_NAME, status)
                        );
                    } else {
                        throw new com.example.notificacionservice.exception.RestClientNonRetryableException(
                                "Client error %s when calling %s".formatted(status, SERVICE_NAME)
                        );
                    }
                })
                .body(UserDto.class);

        Assert.notNull(body, "%s returned null body".formatted(SERVICE_NAME));
        log.info("{} returned {}", SERVICE_NAME, maskIfNeeded("body", body));
        return body;
    }


}

