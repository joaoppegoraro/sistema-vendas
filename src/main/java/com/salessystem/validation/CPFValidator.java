package com.salessystem.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CPFValidator implements ConstraintValidator<CPF, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        String digits = value.replaceAll("[^0-9]", "");

        if (digits.length() != 11) {
            return false;
        }
        // Rejects sequences like 111.111.111-11, which would pass the check-digit calculation below
        if (digits.chars().distinct().count() == 1) {
            return false;
        }

        int firstDigit = calculateCheckDigit(digits, 9);
        if (firstDigit != Character.getNumericValue(digits.charAt(9))) {
            return false;
        }

        int secondDigit = calculateCheckDigit(digits, 10);
        return secondDigit == Character.getNumericValue(digits.charAt(10));
    }

    private int calculateCheckDigit(String digits, int count) {
        int sum = 0;
        int multiplier = count + 1;
        for (int i = 0; i < count; i++) {
            sum += Character.getNumericValue(digits.charAt(i)) * multiplier;
            multiplier--;
        }
        int remainder = sum % 11;
        return (remainder < 2) ? 0 : 11 - remainder;
    }
}
