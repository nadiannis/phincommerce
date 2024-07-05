package com.nadiannis.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResDto {

    private Long id;

    @NotNull(message = "amount is required")
    @Min(value = 0, message = "amount should not be a negative number")
    private Float amount;

    @NotNull(message = "customer_id is required")
    @JsonProperty(value = "customer_id")
    private Long customerId;

}
