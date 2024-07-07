package com.nadiannis.common.dto.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResDto {

    private Long id;

    private String name;

    private Double price;

    private String category;

    @JsonProperty(value = "stock_quantity")
    private Integer stockQuantity;

    private String description;

    @JsonProperty(value = "image_url")
    private String imageUrl;

    @JsonProperty(value = "created_at")
    private LocalDateTime createdAt;

    @JsonProperty(value = "updated_at")
    private LocalDateTime updatedAt;

}
