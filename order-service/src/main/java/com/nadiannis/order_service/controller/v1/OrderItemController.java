package com.nadiannis.order_service.controller.v1;

import com.nadiannis.common.dto.OrderItemResDto;
import com.nadiannis.order_service.dto.SuccessResponse;
import com.nadiannis.order_service.service.OrderItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order-items")
public class OrderItemController {

    private OrderItemService service;

    @Autowired
    public OrderItemController(OrderItemService service) {
        this.service = service;
    }

    @Operation(summary = "Get all order items", responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping
    public Mono<ResponseEntity<?>> getAll() {
        Mono<List<OrderItemResDto>> orderItemsMono = service.getAll().collectList();
        return orderItemsMono.map(data -> {
            String message = "order items retrieved successfully";

            SuccessResponse<List<OrderItemResDto>> response = new SuccessResponse<>(message, data);
            return ResponseEntity.ok(response);
        });
    }

}
