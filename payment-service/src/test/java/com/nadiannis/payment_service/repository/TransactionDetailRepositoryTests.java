package com.nadiannis.payment_service.repository;

import com.nadiannis.payment_service.entity.TransactionDetail;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

@DataR2dbcTest
public class TransactionDetailRepositoryTests {

    @Autowired
    private TransactionDetailRepository repository;

    private TransactionDetail transactionDetail1;

    private TransactionDetail transactionDetail2;

    @BeforeEach
    void setUp() {
        transactionDetail1 = TransactionDetail.builder()
                .orderId(1L)
                .amount(100000F)
                .mode("CASH")
                .status("APPROVED")
                .referenceNumber(UUID.randomUUID().toString())
                .build();
        transactionDetail2 = TransactionDetail.builder()
                .orderId(2L)
                .amount(200000F)
                .mode("BANK_TRANSFER")
                .status("REJECTED")
                .referenceNumber(UUID.randomUUID().toString())
                .build();
    }

    // Flux<TransactionDetail> findAll()
    @Test
    public void TransactionDetailRepository_FindAll_ReturnMoreThanOneTransactionDetails() {
        repository.saveAll(Flux.just(transactionDetail1, transactionDetail2)).blockLast();

        Mono<List<TransactionDetail>> transactionDetailListMono = repository.findAll().collectList();

        StepVerifier.create(transactionDetailListMono)
                .assertNext(transactionDetailList -> {
                    Assertions.assertThat(transactionDetailList).isNotNull();
                    Assertions.assertThat(transactionDetailList.size()).isEqualTo(2);
                })
                .verifyComplete();
    }

    // Mono<TransactionDetail> save(transactionDetail)
    @Test
    public void TransactionDetailRepository_Save_ReturnSavedTransactionDetail() {
        Mono<TransactionDetail> savedTransactionDetailMono = repository.save(transactionDetail1);

        StepVerifier.create(savedTransactionDetailMono)
                .expectNextMatches(transactionDetail -> transactionDetail != null && transactionDetail.getId().equals(transactionDetail1.getId()))
                .verifyComplete();
    }

    // Mono<TransactionDetail> findById(id)
    @Test
    public void TransactionDetailRepository_FindById_ReturnTransactionDetail() {
        repository.save(transactionDetail1).block();

        Mono<TransactionDetail> transactionDetailMono = repository.findById(transactionDetail1.getId());

        StepVerifier.create(transactionDetailMono)
                .expectNextMatches(transactionDetail -> transactionDetail != null && transactionDetail.getId().equals(transactionDetail1.getId()))
                .verifyComplete();
    }

    // Mono<Void> deleteById(id)
    @Test
    public void TransactionDetailRepository_DeleteById_ReturnTransactionDetailIsEmpty() {
        repository.save(transactionDetail1).block();

        Mono<Void> result = repository.deleteById(transactionDetail1.getId());

        StepVerifier.create(result.then(repository.findById(transactionDetail1.getId())))
                .expectNextCount(0L)
                .verifyComplete();
    }

}
