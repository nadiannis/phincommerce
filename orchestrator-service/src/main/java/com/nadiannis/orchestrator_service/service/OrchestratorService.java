package com.nadiannis.orchestrator_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nadiannis.common.dto.MessageDto;
import com.nadiannis.common.dto.ProductResDto;
import com.nadiannis.common.dto.QuantityUpdateReqDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrchestratorService {

    private WebClient webClientProduct;

    private KafkaTemplate<String, Object> kafkaTemplate;

    private ObjectMapper objectMapper;

    @Autowired
    public OrchestratorService(KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper, WebClient webClientProduct) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.webClientProduct = webClientProduct;
    }

    @KafkaListener(topics = "orchestrator", groupId = "phincommerce")
    public void consumeOrchestratorTopic(String message) throws JsonProcessingException {
        MessageDto messageDto = objectMapper.readValue(message, MessageDto.class);
        System.out.println("ORDER_CREATED (deduct the product): " + messageDto);

        if (messageDto.getStatus().equals("ORDER_CREATED")) {
            System.out.println("ORDER_CREATED (deduct the product): " + messageDto);
            System.out.println("Request to PATCH /api/v1/products/{id}/quantities");
        }
    }

}
