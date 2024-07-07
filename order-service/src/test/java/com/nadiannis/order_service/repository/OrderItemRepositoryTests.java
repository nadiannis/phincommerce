package com.nadiannis.order_service.repository;

import com.nadiannis.order_service.entity.Order;
import com.nadiannis.order_service.entity.OrderItem;
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
public class OrderItemRepositoryTests {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    private OrderItem orderItem1;

    private OrderItem orderItem2;

    private Order order;

    @BeforeEach
    void setUp() {
        orderItem1 = OrderItem.builder()
                .orderId(1L)
                .productId(1L)
                .price(10000.0)
                .quantity(10)
                .build();
        orderItem2 = OrderItem.builder()
                .orderId(1L)
                .productId(2L)
                .price(20000.0)
                .quantity(20)
                .build();
        order = Order.builder()
                .paymentMethod("CASH")
                .billingAddress("Bekasi")
                .shippingAddress("Jakarta")
                .orderStatus("CREATED")
                .totalAmount(100000.0)
                .customerId(1L)
                .build();

        orderRepository.deleteAll().block();
        orderItemRepository.deleteAll().block();
    }

    // Flux<OrderItem> findAll()
    @Test
    public void OrderItemRepository_FindAll_ReturnMoreThanOneOrderItems() {
        orderRepository.save(order).block();
        orderItem1.setOrderId(order.getId());
        orderItem2.setOrderId(order.getId());
        orderItemRepository.saveAll(Flux.just(orderItem1, orderItem2)).blockLast();

        Mono<List<OrderItem>> orderItemListMono = orderItemRepository.findAll().collectList();

        StepVerifier.create(orderItemListMono)
                .assertNext(orderItemList -> {
                    Assertions.assertThat(orderItemList).isNotNull();
                    Assertions.assertThat(orderItemList.size()).isEqualTo(2);
                })
                .verifyComplete();
    }

    // Flux<OrderItem> saveAll(orderItems)
    @Test
    public void OrderItemRepository_SaveAll_ReturnSavedOrderItems() {
        orderRepository.save(order).block();
        orderItem1.setOrderId(order.getId());
        orderItem2.setOrderId(order.getId());
        Flux<OrderItem> savedOrderItemListFlux = orderItemRepository.saveAll(Flux.just(orderItem1, orderItem2));

        StepVerifier.create(savedOrderItemListFlux)
                .expectNextMatches(orderItem -> orderItem != null && orderItem.getId().equals(orderItem1.getId()))
                .expectNextMatches(orderItem -> orderItem != null && orderItem.getId().equals(orderItem2.getId()))
                .verifyComplete();
    }

    // Flux<OrderItem> findByOrderId(orderId)
    @Test
    public void OrderItemRepository_FindByOrderId_ReturnOrderItems() {
        orderRepository.save(order).block();
        orderItem1.setOrderId(order.getId());
        orderItem2.setOrderId(order.getId());
        orderItemRepository.saveAll(Flux.just(orderItem1, orderItem2)).blockLast();

        Flux<OrderItem> orderItemListFlux = orderItemRepository.findByOrderId(order.getId());

        StepVerifier.create(orderItemListFlux)
                .expectNextMatches(orderItem -> orderItem != null && orderItem.getOrderId().equals(order.getId()))
                .expectNextMatches(orderItem -> orderItem != null && orderItem.getOrderId().equals(order.getId()))
                .verifyComplete();
    }

}
