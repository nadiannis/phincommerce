package com.nadiannis.common.dto.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
