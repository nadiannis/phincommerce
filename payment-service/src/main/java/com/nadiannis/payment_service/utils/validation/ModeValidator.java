package com.nadiannis.payment_service.utils.validation;

import com.nadiannis.payment_service.utils.Mode;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ModeValidator implements ConstraintValidator<ValidMode, String> {

    @Override
    public void initialize(ValidMode constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        try {
            Mode.valueOf(value.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
