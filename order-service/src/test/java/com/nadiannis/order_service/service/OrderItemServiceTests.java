package com.nadiannis.order_service.service;

import com.nadiannis.common.dto.order.OrderItemResDto;
import com.nadiannis.order_service.entity.OrderItem;
import com.nadiannis.order_service.repository.OrderItemRepository;
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
public class OrderItemServiceTests {

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderItemService orderItemService;

    private OrderItem orderItem1;
    private OrderItem orderItem2;

    @BeforeEach
    void setUp() {
        orderItem1 = OrderItem.builder().id(1L).orderId(1L).productId(1L).price(10000.0).quantity(4).build();
        orderItem2 = OrderItem.builder().id(2L).orderId(1L).productId(2L).price(20000.0).quantity(2).build();
    }

    // Flux<OrderItemResDto> getAll()
    @Test
    public void OrderItemService_GetAll_ReturnOrderItemResDtos() {
        when(orderItemRepository.findAll()).thenReturn(Flux.just(orderItem1, orderItem2));

        Mono<List<OrderItemResDto>> orderItemListMono = orderItemService.getAll().collectList();

        StepVerifier.create(orderItemListMono)
                .assertNext(orderItemList -> {
                    Assertions.assertThat(orderItemList).isNotNull();
                    Assertions.assertThat(orderItemList.size()).isEqualTo(2);
                })
                .verifyComplete();

        verify(orderItemRepository, times(1)).findAll();
    }

}
