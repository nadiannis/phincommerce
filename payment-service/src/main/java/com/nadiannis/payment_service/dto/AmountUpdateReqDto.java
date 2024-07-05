package com.nadiannis.payment_service.dto;

import com.nadiannis.payment_service.utils.validation.ValidAction;
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
public class AmountUpdateReqDto {

    @NotBlank(message = "action is required")
    @ValidAction
    private String action;

    @NotNull(message = "amount is required")
    @Min(value = 0, message = "amount should not be a negative number")
    private Float amount;

}
