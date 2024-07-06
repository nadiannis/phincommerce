package com.nadiannis.common.dto.balance;

import com.nadiannis.common.utils.validation.ValidAmountUpdateAction;
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
    @ValidAmountUpdateAction
    private String action;

    @NotNull(message = "amount is required")
    @Min(value = 0, message = "amount should not be a negative number")
    private Float amount;

}
