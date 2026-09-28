package com.example.walletservice.client.user;

import com.example.walletservice.client.user.response.UserDto;

public interface UserClient {

    UserDto getUser(String userId);
}
