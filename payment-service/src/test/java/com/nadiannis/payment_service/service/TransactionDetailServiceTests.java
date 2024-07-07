package com.nadiannis.payment_service.service;

import com.nadiannis.common.dto.balance.AmountUpdateReqDto;
import com.nadiannis.common.dto.balance.BalanceResDto;
import com.nadiannis.common.dto.transactiondetail.TransactionDetailAddReqDto;
import com.nadiannis.common.dto.transactiondetail.TransactionDetailResDto;
import com.nadiannis.common.utils.Mode;
import com.nadiannis.common.utils.TransactionDetailStatus;
import com.nadiannis.payment_service.dto.TransactionDetailUpdateReqDto;
import com.nadiannis.payment_service.entity.TransactionDetail;
import com.nadiannis.payment_service.repository.TransactionDetailRepository;
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
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionDetailServiceTests {

    @Mock
    private TransactionDetailRepository transactionDetailRepository;

    @Mock
    private BalanceService balanceService;

    @InjectMocks
    private TransactionDetailService transactionDetailService;

    private TransactionDetail transactionDetail1;
    private TransactionDetail transactionDetail2;
    private TransactionDetailAddReqDto transactionDetailAddReqDto;
    private TransactionDetailUpdateReqDto transactionDetailUpdateReqDto;
    private BalanceResDto balanceResDto;

    @BeforeEach
    void setUp() {
        transactionDetail1 = TransactionDetail.builder()
                .id(1L)
                .orderId(1L)
                .amount(100000.0)
                .mode(Mode.CASH.toString())
                .status(TransactionDetailStatus.APPROVED.toString())
                .referenceNumber(UUID.randomUUID().toString())
                .build();
        transactionDetail2 = TransactionDetail.builder()
                .id(2L)
                .orderId(2L)
                .amount(200000.0)
                .mode(Mode.BANK_TRANSFER.toString())
                .status(TransactionDetailStatus.REJECTED.toString())
                .referenceNumber(UUID.randomUUID().toString())
                .build();
        transactionDetailAddReqDto = TransactionDetailAddReqDto.builder()
                .orderId(1L)
                .customerId(1L)
                .amount(100000.0)
                .mode(Mode.CASH.toString())
                .build();
        transactionDetailUpdateReqDto = TransactionDetailUpdateReqDto.builder()
                .orderId(1L)
                .amount(100000.0)
                .mode(Mode.CASH.toString())
                .status(TransactionDetailStatus.APPROVED.toString())
                .referenceNumber(UUID.randomUUID().toString())
                .build();
        balanceResDto = BalanceResDto.builder().id(1L).amount(1000000.0).customerId(1L).build();
    }

    // Flux<TransactionDetailResDto> getAll()
    @Test
    public void TransactionDetailService_GetAll_ReturnTransactionDetailResDtos() {
        when(transactionDetailRepository.findAll()).thenReturn(Flux.just(transactionDetail1, transactionDetail2));

        Mono<List<TransactionDetailResDto>> transactionDetailListMono = transactionDetailService.getAll().collectList();

        StepVerifier.create(transactionDetailListMono)
                .assertNext(transactionDetailList -> {
                    Assertions.assertThat(transactionDetailList).isNotNull();
                    Assertions.assertThat(transactionDetailList.size()).isEqualTo(2);
                })
                .verifyComplete();

        verify(transactionDetailRepository, times(1)).findAll();
    }

    // Mono<TransactionDetailResDto> add(TransactionDetailAddReqDto transactionDetailAddReqDto)
    @Test
    public void TransactionDetailService_Add_SaveAndReturnTransactionDetailResDto() {
        when(balanceService.updateAmount(anyLong(), any(AmountUpdateReqDto.class))).thenReturn(Mono.just(balanceResDto));
        when(transactionDetailRepository.save(any(TransactionDetail.class))).thenReturn(Mono.just(transactionDetail1));

        Mono<TransactionDetailResDto> savedTransactionDetailMono = transactionDetailService.add(transactionDetailAddReqDto);

        StepVerifier.create(savedTransactionDetailMono)
                .assertNext(savedTransactionDetail -> {
                    Assertions.assertThat(savedTransactionDetail).isNotNull();
                    Assertions.assertThat(savedTransactionDetail.getId()).isEqualTo(transactionDetail1.getId());
                    Assertions.assertThat(savedTransactionDetail.getOrderId()).isEqualTo(transactionDetailAddReqDto.getOrderId());
                    Assertions.assertThat(savedTransactionDetail.getAmount()).isEqualTo(transactionDetailAddReqDto.getAmount());
                    Assertions.assertThat(savedTransactionDetail.getMode()).isEqualTo(transactionDetailAddReqDto.getMode());
                    Assertions.assertThat(savedTransactionDetail.getStatus()).isEqualTo(TransactionDetailStatus.APPROVED.toString());
                })
                .verifyComplete();

        verify(balanceService, times(1)).updateAmount(anyLong(), any(AmountUpdateReqDto.class));
        verify(transactionDetailRepository, times(1)).save(any(TransactionDetail.class));
    }

    // Mono<TransactionDetailResDto> getById(Long id)
    @Test
    public void TransactionDetailService_GetById_ReturnTransactionDetailResDto() {
        Long transactionDetail1Id = transactionDetail1.getId();
        when(transactionDetailRepository.findById(anyLong())).thenReturn(Mono.just(transactionDetail1));

        Mono<TransactionDetailResDto> transactionDetailMono = transactionDetailService.getById(transactionDetail1Id);

        StepVerifier.create(transactionDetailMono)
                .expectNextMatches(transactionDetail -> transactionDetail != null && transactionDetail.getId().equals(transactionDetail1Id))
                .verifyComplete();

        verify(transactionDetailRepository, times(1)).findById(anyLong());
    }

    // Mono<TransactionDetailResDto> update(Long id, TransactionDetailUpdateReqDto transactionDetailUpdateReqDto)
    @Test
    public void TransactionDetailService_Update_UpdateAndReturnTransactionDetailResDto() {
        Long transactionDetailId = transactionDetail2.getId();
        transactionDetail1.setId(transactionDetailId);
        transactionDetail1.setReferenceNumber(transactionDetailUpdateReqDto.getReferenceNumber());
        when(transactionDetailRepository.findById(anyLong())).thenReturn(Mono.just(transactionDetail2));
        when(transactionDetailRepository.save(any(TransactionDetail.class))).thenReturn(Mono.just(transactionDetail1));

        Mono<TransactionDetailResDto> updatedTransactionDetailMono = transactionDetailService.update(transactionDetailId, transactionDetailUpdateReqDto);

        StepVerifier.create(updatedTransactionDetailMono)
                .assertNext(updatedTransactionDetail -> {
                    Assertions.assertThat(updatedTransactionDetail).isNotNull();
                    Assertions.assertThat(updatedTransactionDetail.getOrderId()).isEqualTo(transactionDetailUpdateReqDto.getOrderId());
                    Assertions.assertThat(updatedTransactionDetail.getAmount()).isEqualTo(transactionDetailUpdateReqDto.getAmount());
                    Assertions.assertThat(updatedTransactionDetail.getMode()).isEqualTo(transactionDetailUpdateReqDto.getMode());
                    Assertions.assertThat(updatedTransactionDetail.getStatus()).isEqualTo(transactionDetailUpdateReqDto.getStatus());
                    Assertions.assertThat(updatedTransactionDetail.getReferenceNumber()).isEqualTo(transactionDetailUpdateReqDto.getReferenceNumber());
                })
                .verifyComplete();

        verify(transactionDetailRepository, times(1)).findById(anyLong());
        verify(transactionDetailRepository, times(1)).save(any(TransactionDetail.class));
    }

    // Mono<Void> delete(Long id)
    @Test
    public void TransactionDetailService_Delete_ReturnTransactionDetailIsEmpty() {
        Long transactionDetailId = transactionDetail1.getId();
        when(transactionDetailRepository.findById(anyLong())).thenReturn(Mono.just(transactionDetail1));
        when(transactionDetailRepository.deleteById(anyLong())).thenReturn(Mono.empty());

        Mono<Void> result = transactionDetailService.delete(transactionDetailId);

        StepVerifier.create(result).verifyComplete();

        verify(transactionDetailRepository, times(1)).findById(anyLong());
        verify(transactionDetailRepository, times(1)).deleteById(anyLong());
    }

}
