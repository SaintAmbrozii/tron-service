package com.example.walletservice.utils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TronAddressValidator implements ConstraintValidator<TronAddress,String> {


    private static final String TRON_REGEX = "^T[1-9A-HJ-NP-Za-km-z]{33}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        return value.matches(TRON_REGEX);
    }
}
