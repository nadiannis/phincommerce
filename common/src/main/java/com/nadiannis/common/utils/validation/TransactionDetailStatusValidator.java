package com.nadiannis.common.utils.validation;

import com.nadiannis.common.utils.TransactionDetailStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TransactionDetailStatusValidator implements ConstraintValidator<ValidTransactionDetailStatus, String> {

    @Override
    public void initialize(ValidTransactionDetailStatus constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        try {
            TransactionDetailStatus.valueOf(value.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
