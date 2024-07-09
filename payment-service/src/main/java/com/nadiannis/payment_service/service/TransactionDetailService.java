package com.nadiannis.payment_service.service;

import com.nadiannis.common.dto.balance.AmountUpdateReqDto;
import com.nadiannis.common.dto.transactiondetail.TransactionDetailAddReqDto;
import com.nadiannis.common.dto.transactiondetail.TransactionDetailResDto;
import com.nadiannis.common.utils.AmountUpdateAction;
import com.nadiannis.common.utils.TransactionDetailStatus;
import com.nadiannis.payment_service.dto.TransactionDetailUpdateReqDto;
import com.nadiannis.payment_service.entity.TransactionDetail;
import com.nadiannis.common.exception.ResourceNotFoundException;
import com.nadiannis.payment_service.repository.TransactionDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class TransactionDetailService {

    private TransactionDetailRepository repository;

    private BalanceService balanceService;

    @Autowired
    public TransactionDetailService(TransactionDetailRepository repository, BalanceService balanceService) {
        this.repository = repository;
        this.balanceService = balanceService;
    }

    public Flux<TransactionDetailResDto> getAll() {
        return repository.findAll().map(transactionDetail -> mapToResDto(transactionDetail));
    }

    public Mono<TransactionDetailResDto> add(TransactionDetailAddReqDto transactionDetailAddReqDto) {
        return balanceService.updateAmount(
                transactionDetailAddReqDto.getCustomerId(),
                AmountUpdateReqDto.builder()
                        .action(AmountUpdateAction.DEBIT.toString())
                        .amount(transactionDetailAddReqDto.getAmount())
                        .build()
        ).flatMap(balanceResDto -> {
            TransactionDetail transactionDetail = mapFromAddReqDtoToEntity(transactionDetailAddReqDto);
            transactionDetail.setReferenceNumber(UUID.randomUUID().toString());
            transactionDetail.setStatus(TransactionDetailStatus.APPROVED.toString());
            return repository.save(transactionDetail).map(newTransactionDetail -> mapToResDto(newTransactionDetail));
        }).onErrorResume(error -> {
            TransactionDetail transactionDetail = mapFromAddReqDtoToEntity(transactionDetailAddReqDto);
            transactionDetail.setReferenceNumber(UUID.randomUUID().toString());
            transactionDetail.setStatus(TransactionDetailStatus.REJECTED.toString());
            return repository.save(transactionDetail).map(newTransactionDetail -> mapToResDto(newTransactionDetail));
        });
    }

    public Mono<TransactionDetailResDto> getById(Long id) {
        Mono<TransactionDetail> transactionDetailMono = repository
                .findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("transaction", "id", Long.toString(id))));

        return transactionDetailMono.map(transactionDetail -> mapToResDto(transactionDetail));
    }

    public Mono<TransactionDetailResDto> update(Long id, TransactionDetailUpdateReqDto transactionDetailUpdateReqDto) {
        Mono<TransactionDetail> transactionDetailMono = repository
                .findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("transaction", "id", Long.toString(id))));

        return transactionDetailMono
                .flatMap(transactionDetail -> {
                    transactionDetail.setOrderId(transactionDetailUpdateReqDto.getOrderId());
                    transactionDetail.setAmount(transactionDetailUpdateReqDto.getAmount());
                    transactionDetail.setMode(transactionDetailUpdateReqDto.getMode().toUpperCase());
                    if (transactionDetailUpdateReqDto.getStatus() != null) {
                        transactionDetail.setStatus(transactionDetailUpdateReqDto.getStatus().toUpperCase());
                    }
                    if (transactionDetailUpdateReqDto.getReferenceNumber() != null) {
                        transactionDetail.setReferenceNumber(transactionDetailUpdateReqDto.getReferenceNumber());
                    }
                    return repository.save(transactionDetail);
                })
                .map(transactionDetail -> mapToResDto(transactionDetail));
    }

    public Mono<Void> delete(Long id) {
        Mono<TransactionDetail> transactionDetailMono = repository
                .findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("transaction", "id", Long.toString(id))));

        return transactionDetailMono.flatMap(transactionDetail -> repository.deleteById(transactionDetail.getId()));
    }

    private TransactionDetailResDto mapToResDto(TransactionDetail transactionDetail) {
        TransactionDetailResDto transactionDetailResDto = new TransactionDetailResDto();
        transactionDetailResDto.setId(transactionDetail.getId());
        transactionDetailResDto.setOrderId(transactionDetail.getOrderId());
        transactionDetailResDto.setAmount(transactionDetail.getAmount());
        transactionDetailResDto.setMode(transactionDetail.getMode());
        transactionDetailResDto.setStatus(transactionDetail.getStatus());
        transactionDetailResDto.setReferenceNumber(transactionDetail.getReferenceNumber());
        transactionDetailResDto.setPaymentDate(transactionDetail.getPaymentDate());
        return transactionDetailResDto;
    }

    private TransactionDetail mapFromAddReqDtoToEntity(TransactionDetailAddReqDto transactionDetailAddReqDto) {
        TransactionDetail transactionDetail = new TransactionDetail();
        transactionDetail.setOrderId(transactionDetailAddReqDto.getOrderId());
        transactionDetail.setAmount(transactionDetailAddReqDto.getAmount());
        transactionDetail.setMode(transactionDetailAddReqDto.getMode().toUpperCase());
        return transactionDetail;
    }

}
