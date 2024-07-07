package com.nadiannis.order_service.controller.v1;

import com.nadiannis.common.dto.order.OrderItemResDto;
import com.nadiannis.common.dto.order.OrderResDto;
import com.nadiannis.order_service.dto.OrderItemReqDto;
import com.nadiannis.order_service.dto.OrderReqDto;
import com.nadiannis.order_service.dto.StatusUpdateReqDto;
import com.nadiannis.order_service.service.OrderService;
import com.nadiannis.order_service.utils.OrderStatus;
import com.nadiannis.order_service.utils.PaymentMethod;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
public class OrderControllerTests {

    @LocalServerPort
    private int port;

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private StatusUpdateReqDto statusUpdateReqDto;
    private OrderItemReqDto orderItemReqDto;
    private OrderReqDto orderReqDto;
    private OrderItemResDto orderItemResDto1;
    private OrderItemResDto orderItemResDto2;
    private OrderResDto orderResDto1;
    private OrderResDto orderResDto2;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();

        orderItemReqDto = OrderItemReqDto.builder()
                .productId(1L)
                .price(100000.0)
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
        orderItemResDto1 = OrderItemResDto.builder().id(1L).orderId(1L).productId(1L).price(100000.0).quantity(10).build();
        orderItemResDto2 = OrderItemResDto.builder().id(2L).orderId(2L).productId(1L).price(100000.0).quantity(5).build();
        orderResDto1 = OrderResDto.builder()
                .id(1L)
                .paymentMethod(PaymentMethod.CASH.toString())
                .billingAddress("Bekasi")
                .shippingAddress("Jakarta")
                .orderStatus(OrderStatus.CREATED.toString())
                .totalAmount(1000000.0)
                .customerId(1L)
                .orderDate(LocalDateTime.now())
                .orderItems(List.of(orderItemResDto1))
                .build();
        orderResDto2 = OrderResDto.builder()
                .id(2L)
                .paymentMethod(PaymentMethod.BANK_TRANSFER.toString())
                .billingAddress("Bandung")
                .shippingAddress("Semarang")
                .orderStatus(OrderStatus.COMPLETED.toString())
                .totalAmount(500000.0)
                .customerId(2L)
                .orderDate(LocalDateTime.now())
                .orderItems(List.of(orderItemResDto2))
                .build();
    }

    // @GetMapping
    // public Mono<ResponseEntity<?>> getAll()
    @Test
    public void OrderController_GetAll_ReturnOrderResDtos() {
        when(orderService.getAll()).thenReturn(Flux.just(orderResDto1, orderResDto2));

        webTestClient.get().uri("/api/v1/orders")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.message").isEqualTo("orders retrieved successfully")
                .jsonPath("$.data.length()").isEqualTo(2)
                .jsonPath("$.data[0].id").isEqualTo(orderResDto1.getId())
                .jsonPath("$.data[1].id").isEqualTo(orderResDto2.getId())
                .jsonPath("$.data[0].order_items.length()").isEqualTo(1)
                .jsonPath("$.data[1].order_items.length()").isEqualTo(1);

        verify(orderService, times(1)).getAll();
    }

    // @PostMapping
    // public Mono<ResponseEntity<?>> add(@Valid @RequestBody OrderReqDto body)
    @Test
    public void OrderController_Add_ReturnOrderResDto() {
        Double totalAmount = orderReqDto.getOrderItems().get(0).getPrice() * orderReqDto.getOrderItems().get(0).getQuantity();
        when(orderService.add(any(OrderReqDto.class))).thenReturn(Mono.just(orderResDto1));

        webTestClient.post().uri("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(orderReqDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.message").isEqualTo("order added successfully")
                .jsonPath("$.data.id").isEqualTo(orderResDto1.getId())
                .jsonPath("$.data.payment_method").isEqualTo(orderReqDto.getPaymentMethod())
                .jsonPath("$.data.billing_address").isEqualTo(orderReqDto.getBillingAddress())
                .jsonPath("$.data.shipping_address").isEqualTo(orderReqDto.getShippingAddress())
                .jsonPath("$.data.order_status").isEqualTo(OrderStatus.CREATED.toString())
                .jsonPath("$.data.total_amount").isEqualTo(totalAmount)
                .jsonPath("$.data.customer_id").isEqualTo(orderReqDto.getCustomerId())
                .jsonPath("$.data.order_items.length()").isEqualTo(1)
                .jsonPath("$.data.order_items[0].order_id").isEqualTo(orderResDto1.getId());

        verify(orderService, times(1)).add(orderReqDto);
    }

    // @GetMapping("/{id}")
    // public Mono<ResponseEntity<?>> getById(@PathVariable Long id)
    @Test
    public void OrderController_GetById_ReturnOrderResDto() {
        Long orderId = orderResDto1.getId();
        when(orderService.getById(orderId)).thenReturn(Mono.just(orderResDto1));

        webTestClient.get().uri("/api/v1/orders/" + orderId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.message").isEqualTo("order retrieved successfully")
                .jsonPath("$.data.id").isEqualTo(orderId);

        verify(orderService, times(1)).getById(orderId);
    }

    // @PatchMapping("/{id}/status")
    // public Mono<ResponseEntity<?>> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateReqDto body)
    @Test
    public void OrderController_UpdateStatus_UpdateStatusAndReturnOrderResDto() {
        Long orderId = orderResDto1.getId();
        orderResDto1.setOrderStatus(statusUpdateReqDto.getOrderStatus());
        when(orderService.updateStatus(anyLong(), any(StatusUpdateReqDto.class))).thenReturn(Mono.just(orderResDto1));

        webTestClient.patch().uri("/api/v1/orders/"+ orderId +"/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(statusUpdateReqDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.message").isEqualTo("order status updated successfully")
                .jsonPath("$.data.id").isEqualTo(orderResDto1.getId())
                .jsonPath("$.data.order_status").isEqualTo(statusUpdateReqDto.getOrderStatus());

        verify(orderService, times(1)).updateStatus(anyLong(), any(StatusUpdateReqDto.class));
    }

    // @DeleteMapping("/{id}")
    // public Mono<ResponseEntity<?>> delete(@PathVariable Long id)
    @Test
    public void OrderController_DeleteById_ReturnSuccessResponse() {
        Long orderId = orderResDto1.getId();
        when(orderService.delete(orderId)).thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/v1/orders/" + orderId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.message").isEqualTo("order deleted successfully");

        verify(orderService, times(1)).delete(orderId);
    }

}
