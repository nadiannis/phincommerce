package com.nadiannis.payment_service.service;

import com.nadiannis.common.dto.balance.AmountUpdateReqDto;
import com.nadiannis.common.dto.balance.BalanceResDto;
import com.nadiannis.common.utils.AmountUpdateAction;
import com.nadiannis.payment_service.dto.BalanceReqDto;
import com.nadiannis.payment_service.entity.Balance;
import com.nadiannis.payment_service.repository.BalanceRepository;
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
public class BalanceServiceTests {

    @Mock
    private BalanceRepository balanceRepository;

    @InjectMocks
    private BalanceService balanceService;

    private Balance balance1;
    private Balance balance2;
    private BalanceReqDto balanceReqDto;
    private AmountUpdateReqDto amountUpdateReqDto1;
    private AmountUpdateReqDto amountUpdateReqDto2;

    @BeforeEach
    void setUp() {
        balance1 = Balance.builder().id(1L).amount(1000000.0).customerId(1L).build();
        balance2 = Balance.builder().id(2L).amount(2000000.0).customerId(2L).build();
        balanceReqDto = BalanceReqDto.builder().amount(1000000.0).customerId(1L).build();
        amountUpdateReqDto1 = AmountUpdateReqDto.builder().action(AmountUpdateAction.DEBIT.toString()).amount(100000.0).build();
        amountUpdateReqDto2 = AmountUpdateReqDto.builder().action(AmountUpdateAction.CREDIT.toString()).amount(100000.0).build();
    }

    // Flux<BalanceResDto> getAll()
    @Test
    public void BalanceService_GetAll_ReturnBalanceResDtos() {
        when(balanceRepository.findAll()).thenReturn(Flux.just(balance1, balance2));

        Mono<List<BalanceResDto>> balanceListMono = balanceService.getAll().collectList();

        StepVerifier.create(balanceListMono)
                .assertNext(balanceList -> {
                    Assertions.assertThat(balanceList).isNotNull();
                    Assertions.assertThat(balanceList.size()).isEqualTo(2);
                })
                .verifyComplete();

        verify(balanceRepository, times(1)).findAll();
    }

    // Mono<BalanceResDto> add(BalanceReqDto balanceReqDto)
    @Test
    public void BalanceService_Add_SaveAndReturnBalanceResDto() {
        when(balanceRepository.save(any(Balance.class))).thenReturn(Mono.just(balance1));

        Mono<BalanceResDto> savedBalanceMono = balanceService.add(balanceReqDto);

        StepVerifier.create(savedBalanceMono)
                .expectNextMatches(savedBalance -> savedBalance != null && savedBalance.getId().equals(balance1.getId()))
                .verifyComplete();

        verify(balanceRepository, times(1)).save(any(Balance.class));
    }

    // Mono<BalanceResDto> getByCustomerId(Long customerId)
    @Test
    public void BalanceService_GetByCustomerId_ReturnBalanceResDto() {
        Long customerId = balance1.getCustomerId();
        when(balanceRepository.findByCustomerId(anyLong())).thenReturn(Mono.just(balance1));

        Mono<BalanceResDto> balanceMono = balanceService.getByCustomerId(customerId);

        StepVerifier.create(balanceMono)
                .expectNextMatches(balance -> balance != null && balance.getCustomerId().equals(customerId))
                .verifyComplete();

        verify(balanceRepository, times(1)).findByCustomerId(anyLong());
    }

    // Mono<BalanceResDto> updateAmount(Long customerId, AmountUpdateReqDto amountUpdateReqDto)
    @Test
    public void BalanceService_DebitAmount_DebitAmountAndReturnBalanceResDto() {
        Long customerId = balance1.getCustomerId();
        Double updatedAmount = balance1.getAmount() - amountUpdateReqDto1.getAmount();
        when(balanceRepository.findByCustomerId(anyLong())).thenReturn(Mono.just(balance1));
        when(balanceRepository.save(any(Balance.class))).thenReturn(Mono.just(balance1));

        Mono<BalanceResDto> updatedBalanceMono = balanceService.updateAmount(customerId, amountUpdateReqDto1);

        StepVerifier.create(updatedBalanceMono)
                .expectNextMatches(updatedBalance -> updatedBalance != null && updatedBalance.getAmount().equals(updatedAmount))
                .verifyComplete();

        verify(balanceRepository, times(1)).findByCustomerId(anyLong());
        verify(balanceRepository, times(1)).save(any(Balance.class));
    }

    @Test
    public void BalanceService_CreditAmount_CreditAmountAndReturnBalanceResDto() {
        Long customerId = balance1.getCustomerId();
        Double updatedAmount = balance1.getAmount() + amountUpdateReqDto2.getAmount();
        when(balanceRepository.findByCustomerId(anyLong())).thenReturn(Mono.just(balance1));
        when(balanceRepository.save(any(Balance.class))).thenReturn(Mono.just(balance1));

        Mono<BalanceResDto> updatedBalanceMono = balanceService.updateAmount(customerId, amountUpdateReqDto2);

        StepVerifier.create(updatedBalanceMono)
                .expectNextMatches(updatedBalance -> updatedBalance != null && updatedBalance.getAmount().equals(updatedAmount))
                .verifyComplete();

        verify(balanceRepository, times(1)).findByCustomerId(anyLong());
        verify(balanceRepository, times(1)).save(any(Balance.class));
    }

    // Mono<Void> deleteByCustomerId(Long customerId)
    @Test
    public void BalanceService_DeleteByCustomerId_ReturnBalanceIsEmpty() {
        Long customerId = balance1.getCustomerId();
        when(balanceRepository.findByCustomerId(anyLong())).thenReturn(Mono.just(balance1));
        when(balanceRepository.deleteByCustomerId(anyLong())).thenReturn(Mono.empty());

        Mono<Void> result = balanceService.deleteByCustomerId(customerId);

        StepVerifier.create(result).verifyComplete();

        verify(balanceRepository, times(1)).findByCustomerId(anyLong());
        verify(balanceRepository, times(1)).deleteByCustomerId(anyLong());
    }

}
