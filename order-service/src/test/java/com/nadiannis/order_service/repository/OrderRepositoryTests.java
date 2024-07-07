package com.nadiannis.order_service.repository;

import com.nadiannis.order_service.entity.Order;
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
public class OrderRepositoryTests {

    @Autowired
    private OrderRepository repository;

    private Order order1;

    private Order order2;

    @BeforeEach
    void setUp() {
        order1 = Order.builder()
                .paymentMethod("CASH")
                .billingAddress("Bekasi")
                .shippingAddress("Jakarta")
                .orderStatus("CREATED")
                .totalAmount(100000.0)
                .customerId(1L)
                .build();
        order2 = Order.builder()
                .paymentMethod("BANK_TRANSFER")
                .billingAddress("Bandung")
                .shippingAddress("Semarang")
                .orderStatus("COMPLETED")
                .totalAmount(200000.0)
                .customerId(2L)
                .build();

        repository.deleteAll().block();
    }

    // Flux<Order> findAll()
    @Test
    public void OrderRepository_FindAll_ReturnMoreThanOneOrders() {
        repository.saveAll(Flux.just(order1, order2)).blockLast();

        Mono<List<Order>> orderListMono = repository.findAll().collectList();

        StepVerifier.create(orderListMono)
                .assertNext(orderList -> {
                    Assertions.assertThat(orderList).isNotNull();
                    Assertions.assertThat(orderList.size()).isEqualTo(2);
                })
                .verifyComplete();
    }

    // Mono<Order> save(order)
    @Test
    public void OrderRepository_Save_ReturnSavedOrder() {
        Mono<Order> savedOrderMono = repository.save(order1);

        StepVerifier.create(savedOrderMono)
                .expectNextMatches(order -> order != null && order.getId().equals(order1.getId()))
                .verifyComplete();
    }

    // Mono<Order> findById(id)
    @Test
    public void OrderRepository_FindById_ReturnOrder() {
        repository.save(order1).block();

        Mono<Order> orderMono = repository.findById(order1.getId());

        StepVerifier.create(orderMono)
                .expectNextMatches(order -> order != null && order.getId().equals(order1.getId()))
                .verifyComplete();
    }

    // Mono<Void> deleteById(id)
    @Test
    public void OrderRepository_DeleteById_ReturnOrderIsEmpty() {
        repository.save(order1).block();

        Mono<Void> result = repository.deleteById(order1.getId());

        StepVerifier.create(result.then(repository.findById(order1.getId())))
                .expectNextCount(0L)
                .verifyComplete();
    }

}
