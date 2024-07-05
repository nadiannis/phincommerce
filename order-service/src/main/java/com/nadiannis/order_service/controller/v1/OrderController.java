package com.nadiannis.order_service.controller.v1;

import com.nadiannis.order_service.dto.OrderReqDto;
import com.nadiannis.common.dto.OrderResDto;
import com.nadiannis.order_service.dto.StatusUpdateReqDto;
import com.nadiannis.order_service.dto.SuccessResponse;
import com.nadiannis.order_service.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private OrderService service;

    @Autowired
    public OrderController(OrderService service) {
        this.service = service;
    }

    @Operation(summary = "Get all orders", responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping
    public Mono<ResponseEntity<?>> getAll() {
        Mono<List<OrderResDto>> ordersMono = service.getAll().collectList();
        return ordersMono.map(data -> {
            String message = "orders retrieved successfully";

            SuccessResponse<List<OrderResDto>> response = new SuccessResponse<>(message, data);
            return ResponseEntity.ok(response);
        });
    }

    @Operation(summary = "Add a new order", responses = {
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping
    public Mono<ResponseEntity<?>> add(@Valid @RequestBody OrderReqDto body) {
        return service.add(body).map(data -> {
            String message = "order added successfully";

            SuccessResponse<OrderResDto> response = new SuccessResponse<>(message, data);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        });
    }

    @Operation(summary = "Get an order by id", responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{id}")
    public Mono<ResponseEntity<?>> getById(@PathVariable Long id) {
        return service.getById(id).map(data -> {
            String message = "order retrieved successfully";

            SuccessResponse<OrderResDto> response = new SuccessResponse<>(message, data);
            return ResponseEntity.ok(response);
        });
    }

    @Operation(summary = "Update status of an order", responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PatchMapping("/{id}/status")
    public Mono<ResponseEntity<?>> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateReqDto body) {
        return service.updateStatus(id, body).map(data -> {
            String message = "order status updated successfully";

            SuccessResponse<OrderResDto> response = new SuccessResponse<>(message, data);
            return ResponseEntity.ok(response);
        });
    }

    @Operation(summary = "Delete an order by id", responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<?>> delete(@PathVariable Long id) {
        return service
                .delete(id)
                .then(Mono.defer(() -> {
                    String message = "order deleted successfully";

                    SuccessResponse<?> response = new SuccessResponse<>(message);
                    return Mono.just(ResponseEntity.ok(response));
                }));
    }

}
