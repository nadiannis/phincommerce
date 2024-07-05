package com.nadiannis.payment_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nadiannis.payment_service.utils.Mode;
import com.nadiannis.payment_service.utils.Status;
import com.nadiannis.payment_service.utils.validation.ValidMode;
import com.nadiannis.payment_service.utils.validation.ValidStatus;
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
public class TransactionDetailReqDto {

    @NotNull(message = "order_id is required")
    @JsonProperty(value = "order_id")
    private Long orderId;

    @NotNull(message = "amount is required")
    @Min(value = 0, message = "amount should not be a negative number")
    private Float amount;

    @NotBlank(message = "mode is required")
    @ValidMode
    private String mode;

    @NotBlank(message = "status is required")
    @ValidStatus
    private String status;

    @JsonProperty(value = "reference_number")
    private String referenceNumber;

}
