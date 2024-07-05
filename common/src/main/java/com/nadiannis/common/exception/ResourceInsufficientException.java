package com.nadiannis.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
@Getter
public class ResourceInsufficientException extends RuntimeException {

    private String resourceName;
    private String fieldName;

    public ResourceInsufficientException(String resourceName, String fieldName) {
        super(String.format("insufficient %s %s", resourceName, fieldName));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
    }

}
