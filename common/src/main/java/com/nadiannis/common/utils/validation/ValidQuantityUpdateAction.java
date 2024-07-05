package com.nadiannis.common.utils.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = QuantityUpdateActionValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidQuantityUpdateAction {
    String message() default "action should be DEDUCT or ADD";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
