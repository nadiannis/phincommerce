package com.nadiannis.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResDto {

    private Long id;

    @JsonProperty(value = "payment_method")
    private String paymentMethod;

    @JsonProperty(value = "billing_address")
    private String billingAddress;

    @JsonProperty(value = "shipping_address")
    private String shippingAddress;

    @JsonProperty(value = "order_status")
    private String orderStatus;

    @JsonProperty(value = "total_amount")
    private Float totalAmount;

    @JsonProperty(value = "customer_id")
    private Long customerId;

    @JsonProperty(value = "order_date")
    private LocalDateTime orderDate;

    @JsonProperty(value = "order_items")
    private List<OrderItemResDto> orderItems;

}
