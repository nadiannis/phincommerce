package com.nadiannis.product_service.controller.v1;

import com.nadiannis.common.dto.product.ProductResDto;
import com.nadiannis.common.dto.product.QuantityUpdateReqDto;
import com.nadiannis.common.utils.QuantityUpdateAction;
import com.nadiannis.product_service.dto.ProductReqDto;
import com.nadiannis.product_service.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
public class ProductControllerTests {

    @LocalServerPort
    private int port;

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private QuantityUpdateReqDto quantityUpdateReqDto1;
    private QuantityUpdateReqDto quantityUpdateReqDto2;
    private ProductReqDto productReqDto;
    private ProductResDto productResDto1;
    private ProductResDto productResDto2;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();

        quantityUpdateReqDto1 = QuantityUpdateReqDto.builder().action(QuantityUpdateAction.DEDUCT.toString()).stockQuantity(10).build();
        quantityUpdateReqDto2 = QuantityUpdateReqDto.builder().action(QuantityUpdateAction.ADD.toString()).stockQuantity(10).build();
        productReqDto = ProductReqDto.builder()
                .name("product 1")
                .price(100000F)
                .category("category 1")
                .stockQuantity(100)
                .description("description 1")
                .imageUrl("https://images.pexels.com/photos/26110304/pexels-photo-26110304/free-photo-of-parkschloss-leipzig.jpeg")
                .build();
        productResDto1 = ProductResDto.builder()
                .id(1L)
                .name("product 1")
                .price(100000F)
                .category("category 1")
                .stockQuantity(100)
                .description("description 1")
                .imageUrl("https://images.pexels.com/photos/26110304/pexels-photo-26110304/free-photo-of-parkschloss-leipzig.jpeg")
                .build();
        productResDto2 = ProductResDto.builder()
                .id(2L)
                .name("product 2")
                .price(200000F)
                .category("category 2")
                .stockQuantity(200)
                .description("description 2")
                .imageUrl("https://images.pexels.com/photos/26110304/pexels-photo-26110304/free-photo-of-parkschloss-leipzig.jpeg")
                .build();
    }

    // @GetMapping
    // public Mono<ResponseEntity<?>> getAll()
    @Test
    public void ProductController_GetAll_ReturnProductResDtos() {
        when(productService.getAll()).thenReturn(Flux.just(productResDto1, productResDto2));

        webTestClient.get().uri("/api/v1/products")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.message").isEqualTo("products retrieved successfully")
                .jsonPath("$.data.length()").isEqualTo(2)
                .jsonPath("$.data[0].id").isEqualTo(1)
                .jsonPath("$.data[1].id").isEqualTo(2);

        verify(productService, times(1)).getAll();
    }

    // @PostMapping
    // public Mono<ResponseEntity<?>> add(@Valid @RequestBody ProductReqDto body)
    @Test
    public void ProductController_Add_ReturnProductResDto() {
        when(productService.add(any(ProductReqDto.class))).thenReturn(Mono.just(productResDto1));

        webTestClient.post().uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(productReqDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.message").isEqualTo("product added successfully")
                .jsonPath("$.data.id").isEqualTo(productResDto1.getId())
                .jsonPath("$.data.name").isEqualTo(productReqDto.getName())
                .jsonPath("$.data.price").isEqualTo(productReqDto.getPrice())
                .jsonPath("$.data.category").isEqualTo(productReqDto.getCategory())
                .jsonPath("$.data.stock_quantity").isEqualTo(productReqDto.getStockQuantity())
                .jsonPath("$.data.description").isEqualTo(productReqDto.getDescription())
                .jsonPath("$.data.image_url").isEqualTo(productReqDto.getImageUrl());

        verify(productService, times(1)).add(productReqDto);
    }

    // @GetMapping("/{id}")
    // public Mono<ResponseEntity<?>> getById(@PathVariable Long id)
    @Test
    public void ProductController_GetById_ReturnProductResDto() {
        Long productId = productResDto1.getId();
        productResDto1.setId(productId);
        when(productService.getById(productId)).thenReturn(Mono.just(productResDto1));

        webTestClient.get().uri("/api/v1/products/" + productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.message").isEqualTo("product retrieved successfully")
                .jsonPath("$.data.id").isEqualTo(productId);

        verify(productService, times(1)).getById(productId);
    }

    // @PutMapping("/{id}")
    // public Mono<ResponseEntity<?>> update(@PathVariable Long id, @Valid @RequestBody ProductReqDto body)
    @Test
    public void ProductController_Update_ReturnProductResDto() {
        Long productId = productResDto2.getId();
        productResDto1.setId(productId);
        when(productService.update(anyLong(), any(ProductReqDto.class))).thenReturn(Mono.just(productResDto1));

        webTestClient.put().uri("/api/v1/products/" + productId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(productReqDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.message").isEqualTo("product updated successfully")
                .jsonPath("$.data.id").isEqualTo(productId)
                .jsonPath("$.data.name").isEqualTo(productReqDto.getName())
                .jsonPath("$.data.price").isEqualTo(productReqDto.getPrice())
                .jsonPath("$.data.category").isEqualTo(productReqDto.getCategory())
                .jsonPath("$.data.stock_quantity").isEqualTo(productReqDto.getStockQuantity())
                .jsonPath("$.data.description").isEqualTo(productReqDto.getDescription())
                .jsonPath("$.data.image_url").isEqualTo(productReqDto.getImageUrl());

        verify(productService, times(1)).update(anyLong(), any(ProductReqDto.class));
    }

    // @PatchMapping("/{id}/quantities")
    // public Mono<ResponseEntity<?>> updateQuantity(@PathVariable Long id, @Valid @RequestBody QuantityUpdateReqDto quantityUpdateReqDto)
    @Test
    public void ProductController_DeductQuantity_DeductQuantityAndReturnProductResDto() {
        Long productId = productResDto1.getId();
        Integer updatedQuantity = productResDto1.getStockQuantity() - quantityUpdateReqDto1.getStockQuantity();
        productResDto1.setStockQuantity(updatedQuantity);
        when(productService.updateQuantity(anyLong(), any(QuantityUpdateReqDto.class))).thenReturn(Mono.just(productResDto1));

        webTestClient.patch().uri("/api/v1/products/"+ productId +"/quantities")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(quantityUpdateReqDto1)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.message").isEqualTo("product quantity updated successfully")
                .jsonPath("$.data.id").isEqualTo(productId)
                .jsonPath("$.data.stock_quantity").isEqualTo(updatedQuantity);

        verify(productService, times(1)).updateQuantity(anyLong(), any(QuantityUpdateReqDto.class));
    }

    @Test
    public void ProductController_AddQuantity_AddQuantityAndReturnProductResDto() {
        Long productId = productResDto1.getId();
        Integer updatedQuantity = productResDto1.getStockQuantity() + quantityUpdateReqDto1.getStockQuantity();
        productResDto1.setStockQuantity(updatedQuantity);
        when(productService.updateQuantity(anyLong(), any(QuantityUpdateReqDto.class))).thenReturn(Mono.just(productResDto1));

        webTestClient.patch().uri("/api/v1/products/"+ productId +"/quantities")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(quantityUpdateReqDto1)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.message").isEqualTo("product quantity updated successfully")
                .jsonPath("$.data.id").isEqualTo(productId)
                .jsonPath("$.data.stock_quantity").isEqualTo(updatedQuantity);

        verify(productService, times(1)).updateQuantity(anyLong(), any(QuantityUpdateReqDto.class));
    }

    // @DeleteMapping("/{id}")
    // public Mono<ResponseEntity<?>> delete(@PathVariable Long id)
    @Test
    public void ProductController_Delete_ReturnSuccessResponse() {
        Long productId = productResDto1.getId();
        when(productService.delete(productId)).thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/v1/products/" + productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.message").isEqualTo("product deleted successfully");

        verify(productService, times(1)).delete(productId);
    }

}
