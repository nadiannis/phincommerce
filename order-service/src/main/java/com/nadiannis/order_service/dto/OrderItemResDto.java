package com.nadiannis.order_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResDto {

    private Long id;

    @JsonProperty(value = "order_id")
    private Long orderId;

    @JsonProperty(value = "product_id")
    private Long productId;

    private Float price;

    private Integer quantity;

}
