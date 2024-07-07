package com.nadiannis.order_service.service;

import com.nadiannis.common.dto.order.OrderResDto;
import com.nadiannis.order_service.dto.OrderItemReqDto;
import com.nadiannis.order_service.dto.OrderReqDto;
import com.nadiannis.order_service.dto.StatusUpdateReqDto;
import com.nadiannis.order_service.entity.Order;
import com.nadiannis.order_service.entity.OrderItem;
import com.nadiannis.order_service.repository.OrderItemRepository;
import com.nadiannis.order_service.repository.OrderRepository;
import com.nadiannis.order_service.utils.OrderStatus;
import com.nadiannis.order_service.utils.PaymentMethod;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTests {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private OrderService orderService;

    private OrderItemReqDto orderItemReqDto;
    private OrderReqDto orderReqDto;
    private StatusUpdateReqDto statusUpdateReqDto;
    private OrderItem orderItem1;
    private OrderItem orderItem2;
    private Order order1;
    private Order order2;

    @BeforeEach
    void setUp() {
        orderItemReqDto = OrderItemReqDto.builder()
                .productId(1L)
                .price(100000F)
                .quantity(10)
                .build();
        orderReqDto = OrderReqDto.builder()
                .paymentMethod(PaymentMethod.CASH.toString())
                .billingAddress("Bekasi")
                .shippingAddress("Jakarta")
                .customerId(1L)
                .orderItems(List.of(orderItemReqDto))
                .build();
        statusUpdateReqDto = StatusUpdateReqDto.builder().orderStatus(OrderStatus.COMPLETED.toString()).build();
        orderItem1 = OrderItem.builder().id(1L).orderId(1L).productId(1L).price(100000F).quantity(10).build();
        orderItem2 = OrderItem.builder().id(2L).orderId(2L).productId(1L).price(100000F).quantity(5).build();
        order1 = Order.builder()
                .id(1L)
                .paymentMethod(PaymentMethod.CASH.toString())
                .billingAddress("Bekasi")
                .shippingAddress("Jakarta")
                .orderStatus(OrderStatus.CREATED.toString())
                .totalAmount(1000000F)
                .customerId(1L)
                .orderDate(LocalDateTime.now())
                .build();
        order2 = Order.builder()
                .id(2L)
                .paymentMethod(PaymentMethod.BANK_TRANSFER.toString())
                .billingAddress("Bandung")
                .shippingAddress("Semarang")
                .orderStatus(OrderStatus.COMPLETED.toString())
                .totalAmount(500000F)
                .customerId(2L)
                .orderDate(LocalDateTime.now())
                .build();
    }

    // Flux<OrderResDto> getAll()
    @Test
    public void OrderService_GetAll_ReturnOrderResDtos() {
        when(orderRepository.findAll()).thenReturn(Flux.just(order1, order2));
        when(orderItemRepository.findByOrderId(order1.getId())).thenReturn(Flux.just(orderItem1));
        when(orderItemRepository.findByOrderId(order2.getId())).thenReturn(Flux.just(orderItem2));

        Mono<List<OrderResDto>> orderListMono = orderService.getAll().collectList();

        StepVerifier.create(orderListMono)
                .assertNext(orderList -> {
                    Assertions.assertThat(orderList).isNotNull();
                    Assertions.assertThat(orderList.size()).isEqualTo(2);
                    Assertions.assertThat(orderList.get(0).getOrderItems().size()).isEqualTo(1);
                    Assertions.assertThat(orderList.get(1).getOrderItems().size()).isEqualTo(1);
                })
                .verifyComplete();

        verify(orderRepository, times(1)).findAll();
        verify(orderItemRepository, times(2)).findByOrderId(anyLong());
    }

    // Mono<OrderResDto> add(OrderReqDto orderReqDto)
    @Test
    public void OrderService_Add_SaveAndReturnOrderResDto() {
        Float totalAmount = orderReqDto.getOrderItems().get(0).getPrice() * orderReqDto.getOrderItems().get(0).getQuantity();
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(order1));
        when(orderItemRepository.saveAll(anyList())).thenReturn(Flux.just(orderItem1));
        when(kafkaTemplate.send(anyString(), any(Object.class))).thenReturn(null);

        Mono<OrderResDto> orderMono = orderService.add(orderReqDto);

        StepVerifier.create(orderMono)
                .assertNext(order -> {
                    Assertions.assertThat(order).isNotNull();
                    Assertions.assertThat(order.getId()).isEqualTo(order1.getId());
                    Assertions.assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CREATED.toString());
                    Assertions.assertThat(order.getTotalAmount()).isEqualTo(totalAmount);
                    Assertions.assertThat(order.getPaymentMethod()).isEqualTo(orderReqDto.getPaymentMethod());
                    Assertions.assertThat(order.getBillingAddress()).isEqualTo(orderReqDto.getBillingAddress());
                    Assertions.assertThat(order.getShippingAddress()).isEqualTo(orderReqDto.getShippingAddress());
                    Assertions.assertThat(order.getCustomerId()).isEqualTo(orderReqDto.getCustomerId());
                    Assertions.assertThat(order.getOrderItems().size()).isEqualTo(1);
                    Assertions.assertThat(order.getOrderItems().get(0).getOrderId()).isEqualTo(order1.getId());
                })
                .verifyComplete();

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderItemRepository, times(1)).saveAll(anyList());
        verify(kafkaTemplate, times(1)).send(anyString(), any(Object.class));
    }

    // Mono<OrderResDto> getById(Long id)
    @Test
    public void OrderService_GetById_ReturnOrderResDto() {
        Long orderId = order1.getId();
        when(orderRepository.findById(anyLong())).thenReturn(Mono.just(order1));
        when(orderItemRepository.findByOrderId(anyLong())).thenReturn(Flux.just(orderItem1));

        Mono<OrderResDto> orderMono = orderService.getById(orderId);

        StepVerifier.create(orderMono)
                .expectNextMatches(order -> order != null && order.getId().equals(orderId) && order.getOrderItems().get(0).getOrderId().equals(orderId))
                .verifyComplete();

        verify(orderRepository, times(1)).findById(anyLong());
        verify(orderItemRepository, times(1)).findByOrderId(anyLong());
    }

    // Mono<OrderResDto> updateStatus(Long id, StatusUpdateReqDto statusUpdateReqDto)
    @Test
    public void OrderService_UpdateStatus_UpdateStatusAndReturnOrderResDto() {
        Long orderId = order1.getId();
        when(orderRepository.findById(anyLong())).thenReturn(Mono.just(order1));
        order1.setOrderStatus(OrderStatus.COMPLETED.toString());
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(order1));
        when(orderItemRepository.findByOrderId(anyLong())).thenReturn(Flux.just(orderItem1));

        Mono<OrderResDto> updatedOrderMono = orderService.updateStatus(orderId, statusUpdateReqDto);

        StepVerifier.create(updatedOrderMono)
                .expectNextMatches(updatedOrder -> updatedOrder != null && updatedOrder.getOrderStatus().equals(statusUpdateReqDto.getOrderStatus()))
                .verifyComplete();

        verify(orderRepository, times(1)).findById(anyLong());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderItemRepository, times(1)).findByOrderId(anyLong());
    }

    // Mono<Void> delete(Long id)
    @Test
    public void OrderService_Delete_ReturnOrderIsEmpty() {
        Long orderId = order1.getId();
        when(orderRepository.findById(anyLong())).thenReturn(Mono.just(order1));
        when(orderRepository.deleteById(anyLong())).thenReturn(Mono.empty());

        Mono<Void> result = orderService.delete(orderId);

        StepVerifier.create(result).verifyComplete();

        verify(orderRepository, times(1)).findById(anyLong());
        verify(orderRepository, times(1)).deleteById(anyLong());
    }

}
