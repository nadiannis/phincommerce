package com.nadiannis.payment_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(name = "transaction_details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetail {

    @Id
    private Long id;

    private Long orderId;

    private Double amount;

    private String mode;

    private String status;

    private String referenceNumber;

    @CreatedDate
    private LocalDateTime paymentDate;

}
