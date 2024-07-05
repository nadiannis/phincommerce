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

    @Id
    private Long id;

    @JsonProperty(value = "order_id")
    private Long orderId;

    @NotNull(message = "product_id is required")
    @JsonProperty(value = "product_id")
    private Long productId;

    @NotNull(message = "price is required")
    @Min(value = 0, message = "price should not be a negative number")
    private Float price;

    @NotNull(message = "quantity is required")
    @Min(value = 0, message = "quantity should not be a negative number")
    private Integer quantity;

}
