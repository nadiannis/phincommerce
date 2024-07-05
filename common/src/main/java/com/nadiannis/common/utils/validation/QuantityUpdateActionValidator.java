package com.nadiannis.common.utils.validation;

import com.nadiannis.common.utils.QuantityUpdateAction;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class QuantityUpdateActionValidator implements ConstraintValidator<ValidQuantityUpdateAction, String> {

    @Override
    public void initialize(ValidQuantityUpdateAction constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        try {
            QuantityUpdateAction.valueOf(value.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}