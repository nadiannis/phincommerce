package com.nadiannis.payment_service.controller.v1;

import com.nadiannis.payment_service.dto.AmountUpdateReqDto;
import com.nadiannis.payment_service.dto.BalanceReqDto;
import com.nadiannis.payment_service.dto.BalanceResDto;
import com.nadiannis.payment_service.dto.SuccessResponse;
import com.nadiannis.payment_service.service.BalanceService;
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
@RequestMapping("/api/v1/balances")
public class BalanceController {

    private BalanceService service;

    @Autowired
    public BalanceController(BalanceService service) {
        this.service = service;
    }

    @Operation(summary = "Get all balances", responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping
    public Mono<ResponseEntity<?>> getAll() {
        Mono<List<BalanceResDto>> balancesMono = service.getAll().collectList();
        return balancesMono.map(data -> {
            String message = "balances retrieved successfully";

            SuccessResponse<List<BalanceResDto>> response = new SuccessResponse<>(message, data);
            return ResponseEntity.ok(response);
        });
    }

    @Operation(summary = "Create a new balance", responses = {
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping
    public Mono<ResponseEntity<?>> add(@Valid @RequestBody BalanceReqDto body) {
        return service.add(body).map(data -> {
            String message = "balance added successfully";

            SuccessResponse<BalanceResDto> response = new SuccessResponse<>(message, data);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        });
    }

    @Operation(summary = "Get a balance by customer id", responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/customers/{id}")
    public Mono<ResponseEntity<?>> getByCustomerId(@PathVariable Long id) {
        return service.getByCustomerId(id).map(data -> {
            String message = "balance retrieved successfully";

            SuccessResponse<BalanceResDto> response = new SuccessResponse<>(message, data);
            return ResponseEntity.ok(response);
        });
    }

    @Operation(summary = "Update the amount of a customer balance", responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PatchMapping("/customers/{id}/amounts")
    public Mono<ResponseEntity<?>> updateAmount(@PathVariable Long id, @Valid @RequestBody AmountUpdateReqDto body) {
        return service.updateAmount(id, body).map(data -> {
           String message = "balance amount updated successfully";

           SuccessResponse<BalanceResDto> response = new SuccessResponse<>(message, data);
           return ResponseEntity.ok(response);
        });
    }

    @Operation(summary = "Delete a balance by customer id", responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @DeleteMapping("/customers/{id}")
    public Mono<ResponseEntity<?>> deleteByCustomerId(@PathVariable Long id) {
        return service
                .deleteByCustomerId(id)
                .then(Mono.defer(() -> {
                    String message = "balance deleted successfully";

                    SuccessResponse<?> response = new SuccessResponse<>(message);
                    return Mono.just(ResponseEntity.ok(response));
                }));
    }

}
