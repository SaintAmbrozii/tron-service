package com.example.userservice.dto;

import jakarta.persistence.Column;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileDto {


    private String name;

    private String lastName;

    private String email;

    private String phone;
}
