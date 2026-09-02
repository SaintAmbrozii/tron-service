package com.example.userservice.dto;

import jakarta.persistence.Column;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterDto {

    private String name;

    private String lastname;

    private String email;

    private String phone;

}
