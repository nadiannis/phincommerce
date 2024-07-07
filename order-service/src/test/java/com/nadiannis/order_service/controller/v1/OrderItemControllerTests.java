package com.nadiannis.order_service.controller.v1;

import com.nadiannis.common.dto.order.OrderItemResDto;
import com.nadiannis.order_service.service.OrderItemService;
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

import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
public class OrderItemControllerTests {

    @LocalServerPort
    private int port;

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderItemService orderItemService;

    @InjectMocks
    private OrderItemController orderItemController;

    private OrderItemResDto orderItemResDto1;
    private OrderItemResDto orderItemResDto2;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();

        orderItemResDto1 = OrderItemResDto.builder()
                .id(1L)
                .orderId(1L)
                .productId(2L)
                .price(100000F)
                .quantity(10)
                .build();
        orderItemResDto2 = OrderItemResDto.builder()
                .id(2L)
                .orderId(2L)
                .productId(2L)
                .price(100000F)
                .quantity(5)
                .build();
    }

    // @GetMapping
    // public Mono<ResponseEntity<?>> getAll()
    @Test
    public void OrderItemController_GetAll_ReturnOrderItemResDtos() {
        when(orderItemService.getAll()).thenReturn(Flux.just(orderItemResDto1, orderItemResDto2));

        webTestClient.get().uri("/api/v1/order-items")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.message").isEqualTo("order items retrieved successfully")
                .jsonPath("$.data.length()").isEqualTo(2)
                .jsonPath("$.data[0].id").isEqualTo(orderItemResDto1.getId())
                .jsonPath("$.data[1].id").isEqualTo(orderItemResDto2.getId());

        verify(orderItemService, times(1)).getAll();
    }

}
