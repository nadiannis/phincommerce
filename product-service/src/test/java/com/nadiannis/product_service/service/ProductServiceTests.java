package com.nadiannis.product_service.service;

import com.nadiannis.common.dto.product.ProductResDto;
import com.nadiannis.common.dto.product.QuantityUpdateReqDto;
import com.nadiannis.common.utils.QuantityUpdateAction;
import com.nadiannis.product_service.dto.ProductReqDto;
import com.nadiannis.product_service.entity.Product;
import com.nadiannis.product_service.repository.ProductRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTests {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product1;
    private Product product2;
    private ProductReqDto productReqDto;
    private QuantityUpdateReqDto quantityUpdateReqDto1;
    private QuantityUpdateReqDto quantityUpdateReqDto2;

    @BeforeEach
    void setUp() {
        product1 = Product.builder()
                .id(1L)
                .name("product 1")
                .price(100000F)
                .category("category 1")
                .stockQuantity(100)
                .description("description 1")
                .imageUrl("https://images.pexels.com/photos/26110304/pexels-photo-26110304/free-photo-of-parkschloss-leipzig.jpeg")
                .build();
        product2 = Product.builder()
                .id(2L)
                .name("product 2")
                .price(200000F)
                .category("category 2")
                .stockQuantity(200)
                .description("description 2")
                .imageUrl("https://images.pexels.com/photos/26110304/pexels-photo-26110304/free-photo-of-parkschloss-leipzig.jpeg")
                .build();
        productReqDto = ProductReqDto.builder()
                .name("product 1")
                .price(100000F)
                .category("category 1")
                .stockQuantity(100)
                .description("description 1")
                .imageUrl("https://images.pexels.com/photos/26110304/pexels-photo-26110304/free-photo-of-parkschloss-leipzig.jpeg")
                .build();
        quantityUpdateReqDto1 = QuantityUpdateReqDto.builder()
                .stockQuantity(10)
                .action(QuantityUpdateAction.DEDUCT.toString())
                .build();
        quantityUpdateReqDto2 = QuantityUpdateReqDto.builder()
                .stockQuantity(10)
                .action(QuantityUpdateAction.ADD.toString())
                .build();
    }

    // Flux<ProductResDto> getAll()
    @Test
    public void ProductService_GetAll_ReturnProductResDtos() {
        when(productRepository.findAll()).thenReturn(Flux.just(product1, product2));

        Mono<List<ProductResDto>> productListMono = productService.getAll().collectList();

        StepVerifier.create(productListMono)
                .assertNext(productList -> {
                    Assertions.assertThat(productList).isNotNull();
                    Assertions.assertThat(productList.size()).isEqualTo(2);
                })
                .verifyComplete();

        verify(productRepository, times(1)).findAll();
    }

    // Mono<ProductResDto> add(ProductReqDto productReqDto)
    @Test
    public void ProductService_Add_SaveAndReturnProductResDto() {
        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(product1));

        Mono<ProductResDto> savedProductMono = productService.add(productReqDto);

        StepVerifier.create(savedProductMono)
                .expectNextMatches(product -> product != null && product.getId().equals(product1.getId()))
                .verifyComplete();

        verify(productRepository, times(1)).save(any(Product.class));
    }

    // Mono<ProductResDto> getById(Long id)
    @Test
    public void ProductService_GetById_ReturnProductResDto() {
        Long productId = product1.getId();
        when(productRepository.findById(anyLong())).thenReturn(Mono.just(product1));

        Mono<ProductResDto> productMono = productService.getById(productId);

        StepVerifier.create(productMono)
                .expectNextMatches(product -> product != null && product.getId().equals(productId))
                .verifyComplete();

        verify(productRepository, times(1)).findById(anyLong());
    }

    // Mono<ProductResDto> update(Long id, ProductReqDto productReqDto)
    @Test
    public void ProductService_Update_UpdateAndReturnProductResDto() {
        Long productId = product2.getId();
        product1.setId(productId);
        when(productRepository.findById(anyLong())).thenReturn(Mono.just(product2));
        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(product1));

        Mono<ProductResDto> updatedProductMono = productService.update(productId, productReqDto);

        StepVerifier.create(updatedProductMono)
                .assertNext(updatedProduct -> {
                    Assertions.assertThat(updatedProduct).isNotNull();
                    Assertions.assertThat(updatedProduct.getId()).isEqualTo(productId);
                    Assertions.assertThat(updatedProduct.getName()).isEqualTo(product1.getName());
                    Assertions.assertThat(updatedProduct.getPrice()).isEqualTo(product1.getPrice());
                    Assertions.assertThat(updatedProduct.getCategory()).isEqualTo(product1.getCategory());
                    Assertions.assertThat(updatedProduct.getStockQuantity()).isEqualTo(product1.getStockQuantity());
                    Assertions.assertThat(updatedProduct.getDescription()).isEqualTo(product1.getDescription());
                    Assertions.assertThat(updatedProduct.getImageUrl()).isEqualTo(product1.getImageUrl());
                })
                .verifyComplete();

        verify(productRepository, times(1)).findById(anyLong());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    // Mono<ProductResDto> updateQuantity(Long id, QuantityUpdateReqDto quantityUpdateReqDto)
    @Test
    public void ProductService_DeductQuantity_DeductStockQuantityAndReturnProductResDto() {
        Long productId = product1.getId();
        Integer updatedStockQuantity = product1.getStockQuantity() - quantityUpdateReqDto1.getStockQuantity();
        when(productRepository.findById(anyLong())).thenReturn(Mono.just(product1));
        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(product1));

        Mono<ProductResDto> updatedProductMono = productService.updateQuantity(productId, quantityUpdateReqDto1);

        StepVerifier.create(updatedProductMono)
                .expectNextMatches(updatedProduct -> updatedProduct != null && updatedProduct.getStockQuantity().equals(updatedStockQuantity))
                .verifyComplete();

        verify(productRepository, times(1)).findById(anyLong());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    public void ProductService_AddQuantity_AddStockQuantityAndReturnProductResDto() {
        Long productId = product1.getId();
        Integer updatedStockQuantity = product1.getStockQuantity() + quantityUpdateReqDto2.getStockQuantity();
        when(productRepository.findById(anyLong())).thenReturn(Mono.just(product1));
        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(product1));

        Mono<ProductResDto> updatedProductMono = productService.updateQuantity(productId, quantityUpdateReqDto2);

        StepVerifier.create(updatedProductMono)
                .expectNextMatches(updatedProduct -> updatedProduct != null && updatedProduct.getStockQuantity().equals(updatedStockQuantity))
                .verifyComplete();

        verify(productRepository, times(1)).findById(anyLong());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    // Mono<Void> delete(Long id)
    @Test
    public void ProductService_Delete_ReturnProductIsEmpty() {
        Long productId = product1.getId();
        when(productRepository.findById(anyLong())).thenReturn(Mono.just(product1));
        when(productRepository.deleteById(anyLong())).thenReturn(Mono.empty());

        Mono<Void> result = productService.delete(productId);

        StepVerifier.create(result).verifyComplete();

        verify(productRepository, times(1)).findById(anyLong());
        verify(productRepository, times(1)).deleteById(anyLong());
    }

}
