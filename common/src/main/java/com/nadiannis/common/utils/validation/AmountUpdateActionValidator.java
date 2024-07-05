package com.nadiannis.common.utils.validation;

import com.nadiannis.common.utils.AmountUpdateAction;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AmountUpdateActionValidator implements ConstraintValidator<ValidAmountUpdateAction, String> {

    @Override
    public void initialize(ValidAmountUpdateAction constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        try {
            AmountUpdateAction.valueOf(value.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
