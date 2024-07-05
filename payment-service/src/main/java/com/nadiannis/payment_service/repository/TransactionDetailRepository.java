package com.nadiannis.payment_service.repository;

import com.nadiannis.payment_service.entity.TransactionDetail;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionDetailRepository extends R2dbcRepository<TransactionDetail, Long> {

}
