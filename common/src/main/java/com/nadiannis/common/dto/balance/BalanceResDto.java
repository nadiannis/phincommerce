package com.nadiannis.common.dto.balance;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    private Float amount;

    @JsonProperty(value = "customer_id")
    private Long customerId;

}
