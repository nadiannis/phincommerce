package com.nadiannis.payment_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nadiannis.common.utils.validation.ValidMode;
import com.nadiannis.common.utils.validation.ValidTransactionDetailStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetailUpdateReqDto {

    @NotNull(message = "order_id is required")
    @JsonProperty(value = "order_id")
    private Long orderId;

    @NotNull(message = "amount is required")
    @Min(value = 0, message = "amount should not be a negative number")
    private Double amount;

    @NotBlank(message = "mode is required")
    @ValidMode
    private String mode;

    @ValidTransactionDetailStatus
    private String status;

    @JsonProperty(value = "reference_number")
    private String referenceNumber;

}

