package com.nadiannis.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "order_id is required")
    @JsonProperty(value = "order_id")
    private Long orderId;

    @NotNull(message = "amount is required")
    @Min(value = 0, message = "amount should not be a negative number")
    private Float amount;

    @NotBlank(message = "mode is required")
    private String mode;

    @NotBlank(message = "status is required")
    private String status;

    @JsonProperty(value = "reference_number")
    private String referenceNumber;

    @JsonProperty(value = "payment_date")
    private LocalDateTime paymentDate;

}
