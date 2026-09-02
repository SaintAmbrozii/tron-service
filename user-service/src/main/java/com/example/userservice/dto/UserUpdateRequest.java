package com.example.userservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserUpdateRequest {

    private String name;
    private String email;
    private String phone;

}
