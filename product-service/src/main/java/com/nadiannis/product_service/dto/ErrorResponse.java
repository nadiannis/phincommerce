package com.nadiannis.product_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ErrorResponse<T> {

    private String status = "error";
    private LocalDateTime timestamp;
    private String message;
    private T detail;

    public ErrorResponse(LocalDateTime timestamp, String message, T detail) {
        this.timestamp = timestamp;
        this.message = message;
        this.detail = detail;
    }

}
