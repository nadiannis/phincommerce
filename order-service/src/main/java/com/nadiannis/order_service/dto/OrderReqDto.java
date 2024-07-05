package com.nadiannis.order_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nadiannis.order_service.utils.validation.ValidPaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderReqDto {

    @NotBlank(message = "payment_method is required")
    @ValidPaymentMethod
    @JsonProperty(value = "payment_method")
    private String paymentMethod;

    @NotBlank(message = "billing_address is required")
    @JsonProperty(value = "billing_address")
    private String billingAddress;

    @NotBlank(message = "shipping_address is required")
    @JsonProperty(value = "shipping_address")
    private String shippingAddress;

    @NotNull(message = "customer_id is required")
    @JsonProperty(value = "customer_id")
    private Long customerId;

    @Valid
    @NotNull(message = "order_items is required")
    @JsonProperty(value = "order_items")
    private List<OrderItemReqDto> orderItems;

}
