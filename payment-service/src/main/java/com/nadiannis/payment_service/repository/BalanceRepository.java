package com.nadiannis.payment_service.repository;

import com.nadiannis.payment_service.entity.Balance;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface BalanceRepository extends R2dbcRepository<Balance, Long> {

    Mono<Balance> findByCustomerId(Long customerId);

    Mono<Void> deleteByCustomerId(Long customerId);

}
