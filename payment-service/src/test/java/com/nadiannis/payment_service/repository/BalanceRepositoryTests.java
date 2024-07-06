package com.nadiannis.payment_service.repository;

import com.nadiannis.payment_service.entity.Balance;
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
public class BalanceRepositoryTests {

    @Autowired
    private BalanceRepository repository;

    private Balance balance1;

    private Balance balance2;

    @BeforeEach
    void setUp() {
        balance1 = Balance.builder()
                .amount(1000000F)
                .customerId(1L)
                .build();
        balance2 = Balance.builder()
                .amount(2000000F)
                .customerId(2L)
                .build();

        repository.deleteAll().block();
    }

    // Flux<Balance> findAll()
    @Test
    public void BalanceRepository_FindAll_ReturnMoreThanOneBalances() {
        repository.saveAll(Flux.just(balance1, balance2)).blockLast();

        Mono<List<Balance>> balanceListMono = repository.findAll().collectList();

        StepVerifier.create(balanceListMono)
                .assertNext(balanceList -> {
                    Assertions.assertThat(balanceList).isNotNull();
                    Assertions.assertThat(balanceList.size()).isEqualTo(2);
                })
                .verifyComplete();
    }

    // Mono<Balance> save(balance)
    @Test
    public void BalanceRepository_Save_ReturnSavedBalance() {
        Mono<Balance> savedBalanceMono = repository.save(balance1);

        StepVerifier.create(savedBalanceMono)
                .expectNextMatches(balance -> balance != null && balance.getId().equals(balance1.getId()))
                .verifyComplete();
    }

    // Mono<Balance> findByCustomerId(customerId)
    @Test
    public void BalanceRepository_FindByCustomerId_ReturnBalance() {
        repository.save(balance1).block();

        Mono<Balance> balanceMono = repository.findByCustomerId(balance1.getCustomerId());

        StepVerifier.create(balanceMono)
                .expectNextMatches(balance -> balance != null && balance.getCustomerId().equals(balance1.getCustomerId()))
                .verifyComplete();
    }

    // Mono<Void> deleteByCustomerId(customerId)
    @Test
    public void BalanceRepository_DeleteByCustomerId_ReturnBalanceIsEmpty() {
        repository.save(balance1).block();

        Mono<Void> result = repository.deleteByCustomerId(balance1.getCustomerId());

        StepVerifier.create(result.then(repository.findByCustomerId(balance1.getCustomerId())))
                .expectNextCount(0L)
                .verifyComplete();
    }

}
