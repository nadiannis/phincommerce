package com.nadiannis.order_service.utils.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = PaymentMethodValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPaymentMethod {
    String message() default "payment_method should be CASH, CREDIT_CARD, BANK_TRANSFER, or EWALLET";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
