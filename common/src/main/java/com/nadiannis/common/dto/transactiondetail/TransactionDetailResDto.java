package com.nadiannis.common.dto.transactiondetail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetailResDto {

    private Long id;

    @JsonProperty(value = "order_id")
    private Long orderId;

    private Double amount;

    private String mode;

    private String status;

    @JsonProperty(value = "reference_number")
    private String referenceNumber;

    @JsonProperty(value = "payment_date")
    private LocalDateTime paymentDate;

}
