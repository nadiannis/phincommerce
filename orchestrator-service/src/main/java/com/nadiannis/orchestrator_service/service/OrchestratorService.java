package com.nadiannis.orchestrator_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nadiannis.common.dto.MessageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrchestratorService {

    private KafkaTemplate<String, Object> kafkaTemplate;

    private ObjectMapper objectMapper;

    @Autowired
    public OrchestratorService(KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "orchestrator", groupId = "phincommerce")
    public void listenToOrchestratorTopic(String message) throws JsonProcessingException {
        MessageDto messageDto = objectMapper.readValue(message, MessageDto.class);

        if (messageDto.getStatus().equals("ORDER_CREATED")) {
            System.out.println("ORDER_CREATED (deduct the product): " + messageDto);
            kafkaTemplate.send("product", messageDto);
        } else if (messageDto.getStatus().equals("PRODUCT_DEDUCTED")) {
            System.out.println("PRODUCT_DEDUCTED (process the payment): " + messageDto);
            kafkaTemplate.send("payment", messageDto);
        } else if (messageDto.getStatus().equals("PRODUCT_DEDUCTED_FAILED")) {
            System.out.println("PRODUCT_DEDUCTED_FAILED (complete the order): " + messageDto);
            kafkaTemplate.send("order", messageDto);
        } else if (messageDto.getStatus().equals("PAYMENT_APPROVED")) {
            System.out.println("PAYMENT_APPROVED (complete the order): " + messageDto);
            kafkaTemplate.send("order", messageDto);
        } else if (messageDto.getStatus().equals("PAYMENT_REJECTED")) {
            System.out.println("PAYMENT_REJECTED (add the product): " + messageDto);
            kafkaTemplate.send("product", messageDto);
            System.out.println("PAYMENT_REJECTED (complete the order): " + messageDto);
            kafkaTemplate.send("order", messageDto);
        }
    }

}
