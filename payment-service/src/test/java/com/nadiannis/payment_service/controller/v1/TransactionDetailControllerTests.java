package com.nadiannis.payment_service.controller.v1;

import com.nadiannis.common.dto.transactiondetail.TransactionDetailAddReqDto;
import com.nadiannis.common.dto.transactiondetail.TransactionDetailResDto;
import com.nadiannis.common.utils.Mode;
import com.nadiannis.common.utils.TransactionDetailStatus;
import com.nadiannis.payment_service.dto.BalanceReqDto;
import com.nadiannis.payment_service.dto.TransactionDetailUpdateReqDto;
import com.nadiannis.payment_service.service.TransactionDetailService;
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
import java.util.UUID;

import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
public class TransactionDetailControllerTests {

    @LocalServerPort
    private int port;

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private TransactionDetailService transactionDetailService;

    @InjectMocks
    private TransactionDetailController transactionDetailController;

    private TransactionDetailAddReqDto transactionDetailAddReqDto;
    private TransactionDetailUpdateReqDto transactionDetailUpdateReqDto;
    private TransactionDetailResDto transactionDetailResDto1;
    private TransactionDetailResDto transactionDetailResDto2;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();

        transactionDetailAddReqDto = TransactionDetailAddReqDto.builder()
                .orderId(1L)
                .customerId(1L)
                .amount(100000F)
                .mode(Mode.CASH.toString())
                .build();
        transactionDetailUpdateReqDto = TransactionDetailUpdateReqDto.builder()
                .orderId(2L)
                .amount(200000F)
                .mode(Mode.BANK_TRANSFER.toString())
                .status(TransactionDetailStatus.REJECTED.toString())
                .referenceNumber(UUID.randomUUID().toString())
                .build();
        transactionDetailResDto1 = TransactionDetailResDto.builder()
                .id(1L)
                .orderId(1L)
                .amount(100000F)
                .mode(Mode.CASH.toString())
                .status(TransactionDetailStatus.APPROVED.toString())
                .referenceNumber(UUID.randomUUID().toString())
                .paymentDate(LocalDateTime.now())
                .build();
        transactionDetailResDto2 = TransactionDetailResDto.builder()
                .id(2L)
                .orderId(2L)
                .amount(200000F)
                .mode(Mode.BANK_TRANSFER.toString())
                .status(TransactionDetailStatus.REJECTED.toString())
                .referenceNumber(UUID.randomUUID().toString())
                .paymentDate(LocalDateTime.now())
                .build();
    }

    // @GetMapping
    // public Mono<ResponseEntity<?>> getAll()
    @Test
    public void TransactionDetailController_GetAll_ReturnTransactionDetailResDtos() {
        when(transactionDetailService.getAll()).thenReturn(Flux.just(transactionDetailResDto1, transactionDetailResDto2));

        webTestClient.get().uri("/api/v1/transactions")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.message").isEqualTo("transactions retrieved successfully")
                .jsonPath("$.data.length()").isEqualTo(2)
                .jsonPath("$.data[0].id").isEqualTo(transactionDetailResDto1.getId())
                .jsonPath("$.data[1].id").isEqualTo(transactionDetailResDto2.getId());

        verify(transactionDetailService, times(1)).getAll();
    }

    // @PostMapping
    // public Mono<ResponseEntity<?>> add(@Valid @RequestBody TransactionDetailAddReqDto body)
    @Test
    public void TransactionDetailController_Add_ReturnTransactionDetailResDto() {
        when(transactionDetailService.add(any(TransactionDetailAddReqDto.class))).thenReturn(Mono.just(transactionDetailResDto1));

        webTestClient.post().uri("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(transactionDetailAddReqDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.message").isEqualTo("transaction added successfully")
                .jsonPath("$.data.id").isEqualTo(transactionDetailResDto1.getId())
                .jsonPath("$.data.order_id").isEqualTo(transactionDetailAddReqDto.getOrderId())
                .jsonPath("$.data.amount").isEqualTo(transactionDetailAddReqDto.getAmount())
                .jsonPath("$.data.mode").isEqualTo(transactionDetailAddReqDto.getMode())
                .jsonPath("$.data.status").isEqualTo(transactionDetailResDto1.getStatus())
                .jsonPath("$.data.reference_number").isEqualTo(transactionDetailResDto1.getReferenceNumber());

        verify(transactionDetailService, times(1)).add(transactionDetailAddReqDto);
    }

    // @GetMapping("/{id}")
    // public Mono<ResponseEntity<?>> getById(@PathVariable Long id)
    @Test
    public void TransactionDetailController_GetById_ReturnTransactionDetailResDto() {
        Long transactionDetailId = transactionDetailResDto1.getId();
        when(transactionDetailService.getById(transactionDetailId)).thenReturn(Mono.just(transactionDetailResDto1));

        webTestClient.get().uri("/api/v1/transactions/" + transactionDetailId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.message").isEqualTo("transaction retrieved successfully")
                .jsonPath("$.data.id").isEqualTo(transactionDetailId);

        verify(transactionDetailService, times(1)).getById(transactionDetailId);
    }

    // @PutMapping("/{id}")
    // public Mono<ResponseEntity<?>> update(@PathVariable Long id, @Valid @RequestBody TransactionDetailUpdateReqDto body)
    @Test
    public void TransactionDetailController_Update_ReturnTransactionDetailResDto() {
        Long transactionDetailId = transactionDetailResDto1.getId();
        transactionDetailResDto2.setId(transactionDetailId);
        transactionDetailResDto2.setReferenceNumber(transactionDetailUpdateReqDto.getReferenceNumber());
        when(transactionDetailService.update(anyLong(), any(TransactionDetailUpdateReqDto.class))).thenReturn(Mono.just(transactionDetailResDto2));

        webTestClient.put().uri("/api/v1/transactions/" + transactionDetailId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(transactionDetailUpdateReqDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.message").isEqualTo("transaction updated successfully")
                .jsonPath("$.data.id").isEqualTo(transactionDetailId)
                .jsonPath("$.data.order_id").isEqualTo(transactionDetailUpdateReqDto.getOrderId())
                .jsonPath("$.data.amount").isEqualTo(transactionDetailUpdateReqDto.getAmount())
                .jsonPath("$.data.mode").isEqualTo(transactionDetailUpdateReqDto.getMode())
                .jsonPath("$.data.status").isEqualTo(transactionDetailUpdateReqDto.getStatus())
                .jsonPath("$.data.reference_number").isEqualTo(transactionDetailUpdateReqDto.getReferenceNumber());

        verify(transactionDetailService, times(1)).update(anyLong(), any(TransactionDetailUpdateReqDto.class));
    }

    // @DeleteMapping("/{id}")
    // public Mono<ResponseEntity<?>> delete(@PathVariable Long id)
    @Test
    public void TransactionDetailController_Delete_ReturnSuccessResponse() {
        Long transactionDetailId = transactionDetailResDto1.getId();
        when(transactionDetailService.delete(transactionDetailId)).thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/v1/transactions/" + transactionDetailId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.message").isEqualTo("transaction deleted successfully");

        verify(transactionDetailService, times(1)).delete(transactionDetailId);
    }

}
