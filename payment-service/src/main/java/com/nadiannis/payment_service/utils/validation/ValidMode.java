package com.nadiannis.payment_service.utils.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ModeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMode {
    String message() default "mode should be CASH, CREDIT_CARD, BANK_TRANSFER, or EWALLET";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
