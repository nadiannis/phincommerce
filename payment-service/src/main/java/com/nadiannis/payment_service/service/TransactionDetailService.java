package com.nadiannis.payment_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nadiannis.common.dto.MessageDto;
import com.nadiannis.payment_service.dto.AmountUpdateReqDto;
import com.nadiannis.payment_service.dto.TransactionDetailReqDto;
import com.nadiannis.payment_service.dto.TransactionDetailResDto;
import com.nadiannis.payment_service.entity.TransactionDetail;
import com.nadiannis.common.exception.ResourceNotFoundException;
import com.nadiannis.payment_service.repository.TransactionDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class TransactionDetailService {

    private TransactionDetailRepository repository;

    private BalanceService balanceService;

    private KafkaTemplate<String, Object> kafkaTemplate;

    private ObjectMapper objectMapper;

    @Autowired
    public TransactionDetailService(TransactionDetailRepository repository, BalanceService balanceService, KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.repository = repository;
        this.balanceService = balanceService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public Flux<TransactionDetailResDto> getAll() {
        return repository.findAll().map(transactionDetail -> mapToResDto(transactionDetail));
    }

    @KafkaListener(topics = "payment", groupId = "phincommerce")
    public void handlePayment(String message) throws JsonProcessingException {
        MessageDto messageDto = objectMapper.readValue(message, MessageDto.class);

        if (messageDto.getStatus().equals("PRODUCT_DEDUCTED")) {
            System.out.println("PRODUCT_DEDUCTED (process the payment): " + messageDto);
            processPayment(messageDto);
        }
    }

    private void processPayment(MessageDto messageDto) {
        Long customerId = messageDto.getPayload().getCustomerId();
        Float totalAmount = messageDto.getPayload().getTotalAmount();

        balanceService.updateAmount(customerId, AmountUpdateReqDto.builder().action("DEBIT").amount(totalAmount).build())
                .flatMap(balanceResDto -> {
                    TransactionDetailReqDto transactionDetailReqDto = TransactionDetailReqDto.builder()
                            .orderId(messageDto.getPayload().getId())
                            .amount(totalAmount)
                            .mode(messageDto.getPayload().getPaymentMethod())
                            .status("APPROVED")
                            .build();

                    return add(transactionDetailReqDto);
                }).doOnSuccess(transactionDetailResDto -> {
                    kafkaTemplate.send("orchestrator", MessageDto.builder()
                            .status("PAYMENT_APPROVED")
                            .payload(messageDto.getPayload())
                            .build());
                })
                .onErrorResume(error -> {
                    TransactionDetailReqDto transactionDetailReqDto = TransactionDetailReqDto.builder()
                            .orderId(messageDto.getPayload().getId())
                            .amount(totalAmount)
                            .mode(messageDto.getPayload().getPaymentMethod())
                            .status("REJECTED")
                            .build();

                    return add(transactionDetailReqDto).doOnSuccess(transactionDetailResDto -> {
                        kafkaTemplate.send("orchestrator", MessageDto.builder()
                                .status("PAYMENT_REJECTED")
                                .payload(messageDto.getPayload())
                                .build());
                    });
                })
                .subscribe();
    }

    public Mono<TransactionDetailResDto> add(TransactionDetailReqDto transactionDetailReqDto) {
        TransactionDetail transactionDetail = mapToEntity(transactionDetailReqDto);
        transactionDetail.setReferenceNumber(UUID.randomUUID().toString());
        return repository.save(transactionDetail).map(newTransactionDetail -> mapToResDto(newTransactionDetail));
    }

    public Mono<TransactionDetailResDto> getById(Long id) {
        Mono<TransactionDetail> transactionDetailMono = repository
                .findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("transaction", "id", Long.toString(id))));

        return transactionDetailMono.map(transactionDetail -> mapToResDto(transactionDetail));
    }

    public Mono<TransactionDetailResDto> update(Long id, TransactionDetailReqDto transactionDetailReqDto) {
        Mono<TransactionDetail> transactionDetailMono = repository
                .findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("transaction", "id", Long.toString(id))));

        return transactionDetailMono
                .flatMap(transactionDetail -> {
                    transactionDetail.setOrderId(transactionDetailReqDto.getOrderId());
                    transactionDetail.setAmount(transactionDetailReqDto.getAmount());
                    transactionDetail.setMode(transactionDetailReqDto.getMode().toUpperCase());
                    transactionDetail.setStatus(transactionDetailReqDto.getStatus().toUpperCase());
                    if (transactionDetailReqDto.getReferenceNumber() != null) {
                        transactionDetail.setReferenceNumber(transactionDetailReqDto.getReferenceNumber());
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

    private TransactionDetail mapToEntity(TransactionDetailReqDto transactionDetailReqDto) {
        TransactionDetail transactionDetail = new TransactionDetail();
        transactionDetail.setOrderId(transactionDetailReqDto.getOrderId());
        transactionDetail.setAmount(transactionDetailReqDto.getAmount());
        transactionDetail.setMode(transactionDetailReqDto.getMode().toUpperCase());
        transactionDetail.setStatus(transactionDetailReqDto.getStatus().toUpperCase());
        transactionDetail.setReferenceNumber(transactionDetailReqDto.getReferenceNumber());
        return transactionDetail;
    }

}
