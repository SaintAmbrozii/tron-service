package com.example.userservice.dto;

import com.example.userservice.domain.User;
import lombok.Builder;
import lombok.Data;

@Data
public class UserDto{

    private String name;

    private String last_name;

    private String email;

    private String phone;

    public static UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setPhone(user.getPhone());
        dto.setLast_name(user.getLastName());
        return dto;
    }
}
