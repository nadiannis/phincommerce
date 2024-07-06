package com.nadiannis.orchestrator_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nadiannis.common.dto.*;
import com.nadiannis.common.dto.order.OrderItemResDto;
import com.nadiannis.common.dto.product.ProductResDto;
import com.nadiannis.common.dto.product.QuantityUpdateReqDto;
import com.nadiannis.common.dto.transactiondetail.TransactionDetailAddReqDto;
import com.nadiannis.common.dto.transactiondetail.TransactionDetailResDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class OrchestratorService {

    private WebClient webClientProduct;

    private WebClient webClientPayment;

    private KafkaTemplate<String, Object> kafkaTemplate;

    private ObjectMapper objectMapper;

    @Autowired
    public OrchestratorService(WebClient webClientProduct, WebClient webClientPayment, KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.webClientProduct = webClientProduct;
        this.webClientPayment = webClientPayment;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "orchestrator", groupId = "phincommerce")
    public void consumeOrchestratorTopic(String message) throws JsonProcessingException {
        MessageDto messageDto = objectMapper.readValue(message, MessageDto.class);

        if (messageDto.getStatus().equals("ORDER_CREATED")) {
            System.out.println("ORDER_CREATED (deduct the product): " + messageDto);

            Flux.fromIterable(messageDto.getPayload().getOrderItems())
                    .flatMap(orderItem -> deductProduct(orderItem))
                    .collectList()
                    .flatMap(results -> {
                        if (results.contains(false)) {
                            System.out.println("PRODUCT_DEDUCT_FAILED (update the order status)");
                            sendOrderMessage(messageDto, "PRODUCT_DEDUCT_FAILED");
                            return Mono.empty();
                        } else {
                            System.out.println("PRODUCT_DEDUCTED (process to payment)");
                            return processPayment(messageDto);
                        }
                    })
                    .subscribe();
        }
    }

    private Mono<Boolean> deductProduct(OrderItemResDto orderItemResDto) {
        return webClientProduct.patch()
                .uri("/api/v1/products/{id}/quantities", orderItemResDto.getProductId())
                .bodyValue(new QuantityUpdateReqDto("DEDUCT", orderItemResDto.getQuantity()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<SuccessResponse<ProductResDto>>() {})
                .map(productResDto -> true)
                .onErrorResume(error -> Mono.just(false));
    }

    private Mono<Void> processPayment(MessageDto messageDto) {
        TransactionDetailAddReqDto transactionDetailAddReqDto = new TransactionDetailAddReqDto();
        transactionDetailAddReqDto.setOrderId(messageDto.getPayload().getId());
        transactionDetailAddReqDto.setCustomerId(messageDto.getPayload().getCustomerId());
        transactionDetailAddReqDto.setAmount(messageDto.getPayload().getTotalAmount());
        transactionDetailAddReqDto.setMode(messageDto.getPayload().getPaymentMethod());

        return webClientPayment.post()
                .uri("/api/v1/transactions")
                .bodyValue(transactionDetailAddReqDto)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<SuccessResponse<TransactionDetailResDto>>() {})
                .flatMap(resDto -> {
                    if (resDto.getData().getStatus().equals("APPROVED")) {
                        System.out.println("PAYMENT_APPROVED (update the order status): " + resDto.getData());
                        sendOrderMessage(messageDto, "PAYMENT_APPROVED");
                        return Mono.empty();
                    } else if (resDto.getData().getStatus().equals("REJECTED")) {
                        System.out.println("PAYMENT_REJECTED (add the product): " + resDto.getData());
                        return addProducts(messageDto.getPayload().getOrderItems())
                                .then(Mono.fromRunnable(() -> {
                                    System.out.println("PAYMENT_REJECTED (update the order status): " + resDto.getData());
                                    sendOrderMessage(messageDto, "PAYMENT_REJECTED");
                                }));
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> addProducts(List<OrderItemResDto> orderItemResDtoList) {
        return Flux.fromIterable(orderItemResDtoList)
                .flatMap(orderItem -> addProductQuantity(orderItem))
                .then();
    }

    private Mono<Boolean> addProductQuantity(OrderItemResDto orderItemResDto) {
        return webClientProduct.patch()
                .uri("/api/v1/products/{id}/quantities", orderItemResDto.getProductId())
                .bodyValue(new QuantityUpdateReqDto("ADD", orderItemResDto.getQuantity()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<SuccessResponse<ProductResDto>>() {})
                .map(productResDto -> true)
                .onErrorResume(error -> Mono.just(false));
    }

    private void sendOrderMessage(MessageDto messageDto, String status) {
        messageDto.setStatus(status);
        kafkaTemplate.send("order", messageDto);
    }

}
