package com.nadiannis.payment_service.exception;

import com.nadiannis.common.exception.ResourceInsufficientException;
import com.nadiannis.common.exception.ResourceNotFoundException;
import com.nadiannis.common.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.nadiannis.common.utils.Strings.camelToSnake;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    protected Mono<ResponseEntity<Object>> handleWebExchangeBindException(WebExchangeBindException ex, HttpHeaders headers, HttpStatusCode status, ServerWebExchange exchange) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = camelToSnake(((FieldError) error).getField());
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });

        logger.error("WebExchangeBindException", ex);
        ErrorResponse<Map<String, String>> errorResponse = new ErrorResponse<>(LocalDateTime.now(), "invalid request body", errors);
        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Mono<ResponseEntity<ErrorResponse<?>>> handleDuplicateKey(DuplicateKeyException ex, ServerWebExchange exchange) {
        logger.error("DuplicateKeyException", ex);
        ErrorResponse<String> errorResponse = new ErrorResponse<>(LocalDateTime.now(), "resource already exists", exchange.getRequest().getURI().toString());
        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(ResourceInsufficientException.class)
    public Mono<ResponseEntity<ErrorResponse<?>>> handleResourceInsufficient(ResourceInsufficientException ex, ServerWebExchange exchange) {
        logger.error("ResourceInsufficientException", ex);
        ErrorResponse<String> errorResponse = new ErrorResponse<>(LocalDateTime.now(), ex.getMessage(), exchange.getRequest().getURI().toString());
        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse<?>>> handleResourceNotFound(ResourceNotFoundException ex, ServerWebExchange exchange) {
        logger.error("ResourceNotFoundException", ex);
        ErrorResponse<String> errorResponse = new ErrorResponse<>(LocalDateTime.now(), ex.getMessage(), exchange.getRequest().getURI().toString());
        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse<?>>> handleGlobalException(Exception ex, ServerWebExchange exchange) {
        logger.error("Exception", ex);
        ErrorResponse<String> errorResponse = new ErrorResponse<>(LocalDateTime.now(), ex.getMessage(), exchange.getRequest().getURI().toString());
        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));
    }

}
