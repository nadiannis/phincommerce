package com.nadiannis.common.utils.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = AmountUpdateActionValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAmountUpdateAction {
    String message() default "action should be DEBIT or CREDIT";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}