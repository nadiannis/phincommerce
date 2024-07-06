package com.nadiannis.payment_service.service;

import com.nadiannis.common.dto.balance.AmountUpdateReqDto;
import com.nadiannis.payment_service.dto.BalanceReqDto;
import com.nadiannis.common.dto.balance.BalanceResDto;
import com.nadiannis.payment_service.entity.Balance;
import com.nadiannis.common.exception.ResourceInsufficientException;
import com.nadiannis.common.exception.ResourceNotFoundException;
import com.nadiannis.payment_service.repository.BalanceRepository;
import com.nadiannis.common.utils.AmountUpdateAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class BalanceService {

    private BalanceRepository repository;

    @Autowired
    public BalanceService(BalanceRepository repository) {
        this.repository = repository;
    }

    public Flux<BalanceResDto> getAll() {
        return repository.findAll().map(balance -> mapToResDto(balance));
    }

    public Mono<BalanceResDto> add(BalanceReqDto balanceReqDto) {
        Balance balance = mapToEntity(balanceReqDto);
        return repository.save(balance).map(newBalance -> mapToResDto(newBalance));
    }

    public Mono<BalanceResDto> getByCustomerId(Long customerId) {
        Mono<Balance> balanceMono = repository
                .findByCustomerId(customerId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("balance", "customer id", Long.toString(customerId))));

        return balanceMono.map(balance -> mapToResDto(balance));
    }

    public Mono<BalanceResDto> updateAmount(Long customerId, AmountUpdateReqDto amountUpdateReqDto) {
        Mono<Balance> balanceMono = repository
                .findByCustomerId(customerId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("balance", "customer id", Long.toString(customerId))));

        return balanceMono
                .flatMap(balance -> {
                    if (amountUpdateReqDto.getAction().toUpperCase().equals(AmountUpdateAction.DEBIT.toString())) {
                        if (balance.getAmount() >= amountUpdateReqDto.getAmount()) {
                            balance.setAmount(balance.getAmount() - amountUpdateReqDto.getAmount());
                        } else {
                            return Mono.error(new ResourceInsufficientException("balance", "amount"));
                        }
                    } else if (amountUpdateReqDto.getAction().toUpperCase().equals(AmountUpdateAction.CREDIT.toString())) {
                        balance.setAmount(balance.getAmount() + amountUpdateReqDto.getAmount());
                    }
                    return repository.save(balance);
                })
                .map(balance -> mapToResDto(balance));
    }

    public Mono<Void> deleteByCustomerId(Long customerId) {
        Mono<Balance> balanceMono = repository
                .findByCustomerId(customerId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("balance", "customer id", Long.toString(customerId))));

        return balanceMono.flatMap(balance -> repository.deleteByCustomerId(balance.getCustomerId()));
    }

    private BalanceResDto mapToResDto(Balance balance) {
        BalanceResDto balanceResDto = new BalanceResDto();
        balanceResDto.setId(balance.getId());
        balanceResDto.setAmount(balance.getAmount());
        balanceResDto.setCustomerId(balance.getCustomerId());
        return balanceResDto;
    }

    private Balance mapToEntity(BalanceReqDto balanceReqDto) {
        Balance balance = new Balance();
        balance.setAmount(balanceReqDto.getAmount());
        balance.setCustomerId(balanceReqDto.getCustomerId());
        return balance;
    }

}
