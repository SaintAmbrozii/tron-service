package com.example.notificacionservice.client.user;

import com.example.notificacionservice.client.user.response.UserDto;

public interface UserClient {

    UserDto getUser(String userId);
}
