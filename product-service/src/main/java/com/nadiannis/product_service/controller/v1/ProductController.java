package com.nadiannis.product_service.controller.v1;

import com.nadiannis.product_service.dto.ProductReqDto;
import com.nadiannis.product_service.dto.ProductResDto;
import com.nadiannis.product_service.dto.QuantityUpdateReqDto;
import com.nadiannis.product_service.dto.SuccessResponse;
import com.nadiannis.product_service.service.ProductService;
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
@RequestMapping("/api/v1/products")
public class ProductController {

    private ProductService service;

    @Autowired
    public ProductController(ProductService service) {
        this.service = service;
    }

    @Operation(summary = "Get all products", responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping
    public Mono<ResponseEntity<?>> getAll() {
        Mono<List<ProductResDto>> productsMono = service.getAll().collectList();
        return productsMono.map(data -> {
            String message = "products retrieved successfully";

            SuccessResponse<List<ProductResDto>> response = new SuccessResponse<>(message, data);
            return ResponseEntity.ok(response);
        });
    }

    @Operation(summary = "Add a new product", responses = {
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping
    public Mono<ResponseEntity<?>> add(@Valid @RequestBody ProductReqDto body) {
        return service.add(body).map(data -> {
            String message = "product added successfully";

            SuccessResponse<ProductResDto> response = new SuccessResponse<>(message, data);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        });
    }

    @Operation(summary = "Get a product by id", responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{id}")
    public Mono<ResponseEntity<?>> getById(@PathVariable Long id) {
        return service.getById(id).map(data -> {
            String message = "product retrieved successfully";

            SuccessResponse<ProductResDto> response = new SuccessResponse<>(message, data);
            return ResponseEntity.ok(response);
        });
    }

    @Operation(summary = "Update a product by id", responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PutMapping("/{id}")
    public Mono<ResponseEntity<?>> update(@PathVariable Long id, @Valid @RequestBody ProductReqDto body) {
        return service.update(id, body).map(data -> {
            String message = "product updated successfully";

            SuccessResponse<ProductResDto> response = new SuccessResponse<>(message, data);
            return ResponseEntity.ok(response);
        });
    }

    @Operation(summary = "Update quantity of a product", responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PatchMapping("/{id}/quantities")
    public Mono<ResponseEntity<?>> updateQuantity(@PathVariable Long id, @Valid @RequestBody QuantityUpdateReqDto quantityUpdateReqDto) {
        return service.updateQuantity(id, quantityUpdateReqDto).map(data -> {
            String message = "product quantity updated successfully";

            SuccessResponse<ProductResDto> response = new SuccessResponse<>(message, data);
            return ResponseEntity.ok(response);
        });
    }

    @Operation(summary = "Delete a product by id", responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<?>> delete(@PathVariable Long id) {
        return service
                .delete(id)
                .then(Mono.defer(() -> {
                    String message = "product deleted successfully";

                    SuccessResponse<?> response = new SuccessResponse<>(message);
                    return Mono.just(ResponseEntity.ok(response));
                }));
    }

}
