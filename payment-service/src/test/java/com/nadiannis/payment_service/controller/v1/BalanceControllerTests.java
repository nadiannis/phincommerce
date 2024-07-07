package com.nadiannis.payment_service.controller.v1;

import com.nadiannis.common.dto.balance.AmountUpdateReqDto;
import com.nadiannis.common.dto.balance.BalanceResDto;
import com.nadiannis.common.utils.AmountUpdateAction;
import com.nadiannis.payment_service.dto.BalanceReqDto;
import com.nadiannis.payment_service.service.BalanceService;
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
public class BalanceControllerTests {

    @LocalServerPort
    private int port;

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private BalanceService balanceService;

    @InjectMocks
    private BalanceController balanceController;

    private AmountUpdateReqDto amountUpdateReqDto1;
    private AmountUpdateReqDto amountUpdateReqDto2;
    private BalanceReqDto balanceReqDto;
    private BalanceResDto balanceResDto1;
    private BalanceResDto balanceResDto2;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();

        amountUpdateReqDto1 = AmountUpdateReqDto.builder().action(AmountUpdateAction.DEBIT.toString()).amount(100000.0).build();
        amountUpdateReqDto2 = AmountUpdateReqDto.builder().action(AmountUpdateAction.CREDIT.toString()).amount(100000.0).build();
        balanceReqDto = BalanceReqDto.builder().amount(1000000.0).customerId(1L).build();
        balanceResDto1 = BalanceResDto.builder().id(1L).amount(1000000.0).customerId(1L).build();
        balanceResDto2 = BalanceResDto.builder().id(2L).amount(2000000.0).customerId(2L).build();
    }

    // @GetMapping
    // public Mono<ResponseEntity<?>> getAll()
    @Test
    public void BalanceController_GetAll_ReturnBalanceResDtos() {
        when(balanceService.getAll()).thenReturn(Flux.just(balanceResDto1, balanceResDto2));

        webTestClient.get().uri("/api/v1/balances")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.message").isEqualTo("balances retrieved successfully")
                .jsonPath("$.data.length()").isEqualTo(2)
                .jsonPath("$.data[0].id").isEqualTo(balanceResDto1.getId())
                .jsonPath("$.data[1].id").isEqualTo(balanceResDto2.getId());

        verify(balanceService, times(1)).getAll();
    }

    // @PostMapping
    // public Mono<ResponseEntity<?>> add(@Valid @RequestBody BalanceReqDto body)
    @Test
    public void BalanceController_Add_ReturnBalanceResDto() {
        when(balanceService.add(any(BalanceReqDto.class))).thenReturn(Mono.just(balanceResDto1));

        webTestClient.post().uri("/api/v1/balances")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(balanceReqDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.message").isEqualTo("balance added successfully")
                .jsonPath("$.data.id").isEqualTo(balanceResDto1.getId())
                .jsonPath("$.data.amount").isEqualTo(balanceReqDto.getAmount())
                .jsonPath("$.data.customer_id").isEqualTo(balanceReqDto.getCustomerId());

        verify(balanceService, times(1)).add(balanceReqDto);
    }

    // @GetMapping("/customers/{id}")
    // public Mono<ResponseEntity<?>> getByCustomerId(@PathVariable Long id)
    @Test
    public void BalanceController_GetByCustomerId_ReturnBalanceResDto() {
        Long customerId = balanceResDto1.getCustomerId();
        when(balanceService.getByCustomerId(customerId)).thenReturn(Mono.just(balanceResDto1));

        webTestClient.get().uri("/api/v1/balances/customers/" + customerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.message").isEqualTo("balance retrieved successfully")
                .jsonPath("$.data.id").isEqualTo(balanceResDto1.getId())
                .jsonPath("$.data.customer_id").isEqualTo(customerId);

        verify(balanceService, times(1)).getByCustomerId(customerId);
    }

    // @PatchMapping("/customers/{id}/amounts")
    // public Mono<ResponseEntity<?>> updateAmount(@PathVariable Long id, @Valid @RequestBody AmountUpdateReqDto body)
    @Test
    public void BalanceController_DebitAmount_DebitAmountAndReturnBalanceResDto() {
        Long customerId = balanceResDto1.getCustomerId();
        Double updatedAmount = balanceResDto1.getAmount() - amountUpdateReqDto1.getAmount();
        balanceResDto1.setAmount(updatedAmount);
        when(balanceService.updateAmount(anyLong(), any(AmountUpdateReqDto.class))).thenReturn(Mono.just(balanceResDto1));

        webTestClient.patch().uri("/api/v1/balances/customers/"+ customerId +"/amounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(amountUpdateReqDto1)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.message").isEqualTo("balance amount updated successfully")
                .jsonPath("$.data.id").isEqualTo(balanceResDto1.getId())
                .jsonPath("$.data.amount").isEqualTo(updatedAmount);

        verify(balanceService, times(1)).updateAmount(anyLong(), any(AmountUpdateReqDto.class));
    }

    @Test
    public void BalanceController_CreditAmount_CreditAmountAndReturnBalanceResDto() {
        Long customerId = balanceResDto1.getCustomerId();
        Double updatedAmount = balanceResDto1.getAmount() + amountUpdateReqDto1.getAmount();
        balanceResDto1.setAmount(updatedAmount);
        when(balanceService.updateAmount(anyLong(), any(AmountUpdateReqDto.class))).thenReturn(Mono.just(balanceResDto1));

        webTestClient.patch().uri("/api/v1/balances/customers/"+ customerId +"/amounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(amountUpdateReqDto1)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.message").isEqualTo("balance amount updated successfully")
                .jsonPath("$.data.id").isEqualTo(balanceResDto1.getId())
                .jsonPath("$.data.amount").isEqualTo(updatedAmount);

        verify(balanceService, times(1)).updateAmount(anyLong(), any(AmountUpdateReqDto.class));
    }

    // @DeleteMapping("/customers/{id}")
    // public Mono<ResponseEntity<?>> deleteByCustomerId(@PathVariable Long id)
    @Test
    public void BalanceController_DeleteByCustomerId_ReturnSuccessResponse() {
        Long customerId = balanceResDto1.getCustomerId();
        when(balanceService.deleteByCustomerId(customerId)).thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/v1/balances/customers/" + customerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.message").isEqualTo("balance deleted successfully");

        verify(balanceService, times(1)).deleteByCustomerId(customerId);
    }

}
