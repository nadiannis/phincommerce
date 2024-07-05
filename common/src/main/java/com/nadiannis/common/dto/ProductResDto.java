package com.nadiannis.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank(message = "name is required")
    private String name;

    @NotNull(message = "price is required")
    @Min(value = 0, message = "price should not be a negative number")
    private Float price;

    @NotBlank(message = "category is required")
    private String category;

    @NotNull(message = "stock_quantity is required")
    @Min(value = 0, message = "stock_quantity should not be a negative number")
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
