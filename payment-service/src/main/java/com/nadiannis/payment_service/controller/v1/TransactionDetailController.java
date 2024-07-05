package com.nadiannis.payment_service.controller.v1;

import com.nadiannis.payment_service.dto.SuccessResponse;
import com.nadiannis.payment_service.dto.TransactionDetailReqDto;
import com.nadiannis.payment_service.dto.TransactionDetailResDto;
import com.nadiannis.payment_service.service.TransactionDetailService;
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
@RequestMapping("/api/v1/transactions")
public class TransactionDetailController {

    private TransactionDetailService service;

    @Autowired
    public TransactionDetailController(TransactionDetailService service) {
        this.service = service;
    }

    @Operation(summary = "Get all transactions", responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping
    public Mono<ResponseEntity<?>> getAll() {
        Mono<List<TransactionDetailResDto>> transactionsMono = service.getAll().collectList();
        return transactionsMono.map(data -> {
            String message = "transactions retrieved successfully";

            SuccessResponse<List<TransactionDetailResDto>> response = new SuccessResponse<>(message, data);
            return ResponseEntity.ok(response);
        });
    }

    @Operation(summary = "Create a new transaction", responses = {
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping
    public Mono<ResponseEntity<?>> add(@Valid @RequestBody TransactionDetailReqDto body) {
        return service.add(body).map(data -> {
            String message = "transaction added successfully";

            SuccessResponse<TransactionDetailResDto> response = new SuccessResponse<>(message, data);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        });
    }

    @Operation(summary = "Get a transaction by id", responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{id}")
    public Mono<ResponseEntity<?>> getById(@PathVariable Long id) {
        return service.getById(id).map(data -> {
            String message = "transaction retrieved successfully";

            SuccessResponse<TransactionDetailResDto> response = new SuccessResponse<>(message, data);
            return ResponseEntity.ok(response);
        });
    }

    @Operation(summary = "Update a transaction by id", responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PutMapping("/{id}")
    public Mono<ResponseEntity<?>> update(@PathVariable Long id, @Valid @RequestBody TransactionDetailReqDto body) {
        return service.update(id, body).map(data -> {
            String message = "transaction updated successfully";

            SuccessResponse<TransactionDetailResDto> response = new SuccessResponse<>(message, data);
            return ResponseEntity.ok(response);
        });
    }

    @Operation(summary = "Delete a transaction by id", responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<?>> delete(@PathVariable Long id) {
        return service
                .delete(id)
                .then(Mono.defer(() -> {
                    String message = "transaction deleted successfully";

                    SuccessResponse<?> response = new SuccessResponse<>(message);
                    return Mono.just(ResponseEntity.ok(response));
                }));
    }

}
