package com.nadiannis.payment_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuccessResponse<T> {

    private String status = "success";
    private String message;
    private T data;

    public SuccessResponse(String message) {
        this.message = message;
    }

    public SuccessResponse(String message, T data) {
        this.message = message;
        this.data = data;
    }

}
