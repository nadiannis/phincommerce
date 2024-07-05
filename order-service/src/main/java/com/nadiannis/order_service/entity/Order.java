package com.nadiannis.order_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    private Long id;

    private String paymentMethod;

    private String billingAddress;

    private String shippingAddress;

    private String orderStatus;

    private Float totalAmount;

    private Long customerId;

    @CreatedDate
    private LocalDateTime orderDate;

}
