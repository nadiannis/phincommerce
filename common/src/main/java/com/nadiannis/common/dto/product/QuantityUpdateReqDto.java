package com.nadiannis.common.dto.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nadiannis.common.utils.validation.ValidQuantityUpdateAction;
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
public class QuantityUpdateReqDto {

    @NotBlank(message = "action is required")
    @ValidQuantityUpdateAction
    private String action;

    @NotNull(message = "stock_quantity is required")
    @Min(value = 0, message = "stock_quantity should not be a negative number")
    @JsonProperty(value = "stock_quantity")
    private Integer stockQuantity;

}
