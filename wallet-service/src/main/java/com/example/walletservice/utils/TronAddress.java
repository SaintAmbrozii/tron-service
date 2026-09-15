package com.example.walletservice.utils;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TronAddressValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface TronAddress {
    String message() default "Неверный формат TRON адреса";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

