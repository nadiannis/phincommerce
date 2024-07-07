package com.nadiannis.product_service.repository;

import com.nadiannis.product_service.entity.Product;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

@DataR2dbcTest
public class ProductRepositoryTests {

    @Autowired
    private ProductRepository repository;

    private Product product1;

    private Product product2;

    @BeforeEach
    void setUp() {
        product1 = Product.builder()
                .name("product 1")
                .price(100000.0)
                .category("category 1")
                .stockQuantity(100)
                .description("description 1")
                .imageUrl("https://images.pexels.com/photos/26110304/pexels-photo-26110304/free-photo-of-parkschloss-leipzig.jpeg")
                .build();
        product2 = Product.builder()
                .name("product 2")
                .price(200000.0)
                .category("category 2")
                .stockQuantity(200)
                .description("description 2")
                .imageUrl("https://images.pexels.com/photos/26110304/pexels-photo-26110304/free-photo-of-parkschloss-leipzig.jpeg")
                .build();

        repository.deleteAll().block();
    }

    // Flux<Product> findAll()
    @Test
    public void ProductRepository_FindAll_ReturnMoreThanOneProducts() {
        repository.saveAll(Flux.just(product1, product2)).blockLast();

        Mono<List<Product>> productListMono = repository.findAll().collectList();

        StepVerifier.create(productListMono)
                .assertNext(productList -> {
                    Assertions.assertThat(productList).isNotNull();
                    Assertions.assertThat(productList.size()).isEqualTo(2);
                })
                .verifyComplete();
    }

    // Mono<Product> save(product)
    @Test
    public void ProductRepository_Save_ReturnSavedProduct() {
        Mono<Product> savedProductMono = repository.save(product1);

        StepVerifier.create(savedProductMono)
                .expectNextMatches(product -> product != null && product.getName().equals("product 1"))
                .verifyComplete();
    }

    // Mono<Product> findById(id)
    @Test
    public void ProductRepository_FindById_ReturnProduct() {
        repository.save(product1).block();

        Mono<Product> productMono = repository.findById(product1.getId());

        StepVerifier.create(productMono)
                .expectNextMatches(product -> product != null && product.getName().equals("product 1"))
                .verifyComplete();
    }

    // Mono<Void> deleteById(id)
    @Test
    public void ProductRepository_DeleteById_ReturnProductIsEmpty() {
        repository.save(product1).block();

        Mono<Void> result = repository.deleteById(product1.getId());

        StepVerifier.create(result.then(repository.findById(product1.getId())))
                .expectNextCount(0L)
                .verifyComplete();
    }

}
