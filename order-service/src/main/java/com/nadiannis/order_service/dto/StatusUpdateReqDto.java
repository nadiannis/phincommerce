package com.nadiannis.order_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nadiannis.order_service.utils.validation.ValidOrderStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateReqDto {

    @NotBlank(message = "order_status is required")
    @ValidOrderStatus
    @JsonProperty(value = "order_status")
    private String orderStatus;

}
