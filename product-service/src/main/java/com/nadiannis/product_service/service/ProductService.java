package com.nadiannis.product_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nadiannis.common.dto.MessageDto;
import com.nadiannis.product_service.dto.ProductReqDto;
import com.nadiannis.product_service.dto.ProductResDto;
import com.nadiannis.product_service.dto.QuantityUpdateReqDto;
import com.nadiannis.product_service.entity.Product;
import com.nadiannis.common.exception.ResourceInsufficientException;
import com.nadiannis.common.exception.ResourceNotFoundException;
import com.nadiannis.product_service.repository.ProductRepository;
import com.nadiannis.product_service.utils.Action;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private ProductRepository repository;

    private KafkaTemplate<String, Object> kafkaTemplate;

    private ObjectMapper objectMapper;

    @Autowired
    public ProductService(ProductRepository repository, KafkaTemplate kafkaTemplate, ObjectMapper objectMapper) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "product", groupId = "phincommerce")
    public void handleProductDeduct(String message) throws JsonProcessingException {
        MessageDto messageDto = objectMapper.readValue(message, MessageDto.class);

        if (messageDto.getStatus().equals("ORDER_CREATED")) {
            System.out.println("ORDER_CREATED (deduct the product):" + messageDto);

            List<Mono<ProductResDto>> updateMonos = messageDto.getPayload().getOrderItems().stream()
                    .map(orderItemResDto -> updateQuantity(
                            orderItemResDto.getProductId(),
                            QuantityUpdateReqDto.builder()
                                    .stockQuantity(orderItemResDto.getQuantity())
                                    .action("DEDUCT")
                                    .build()
                    ))
                    .collect(Collectors.toList());

            Mono.when(updateMonos)
                    .then(Mono.defer(() -> Mono.fromFuture(() -> kafkaTemplate.send("orchestrator", MessageDto.builder()
                            .status("PRODUCT_DEDUCTED")
                            .payload(messageDto.getPayload())
                            .build()))))
                    .onErrorResume(error -> Mono.fromFuture(() -> kafkaTemplate.send("orchestrator", MessageDto.builder()
                            .status("PRODUCT_DEDUCTED_FAILED")
                            .payload(messageDto.getPayload())
                            .build())))
                    .subscribe();
        } else if (messageDto.getStatus().equals("PAYMENT_REJECTED")) {
            System.out.println("PAYMENT_REJECTED (add the product):" + messageDto);

            List<Mono<ProductResDto>> updateMonos = messageDto.getPayload().getOrderItems().stream()
                    .map(orderItemResDto -> updateQuantity(
                            orderItemResDto.getProductId(),
                            QuantityUpdateReqDto.builder()
                                    .stockQuantity(orderItemResDto.getQuantity())
                                    .action("ADD")
                                    .build()
                    ))
                    .collect(Collectors.toList());

            Mono.when(updateMonos)
                    .then(Mono.defer(() -> Mono.fromFuture(() -> kafkaTemplate.send("orchestrator", MessageDto.builder()
                            .status("PRODUCT_ADDED")
                            .payload(messageDto.getPayload())
                            .build()))))
                    .onErrorResume(error -> Mono.fromFuture(() -> kafkaTemplate.send("orchestrator", MessageDto.builder()
                            .status("PRODUCT_ADD_FAILED")
                            .payload(messageDto.getPayload())
                            .build())))
                    .subscribe();
        }
    }

    public Flux<ProductResDto> getAll() {
        return repository.findAll().map(product -> mapToResDto(product));
    }

    public Mono<ProductResDto> add(ProductReqDto productReqDto) {
        Product product = mapToEntity(productReqDto);
        return repository.save(product).map(newProduct -> mapToResDto(newProduct));
    }

    public Mono<ProductResDto> getById(Long id) {
        Mono<Product> productMono = repository
                .findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("product", "id", Long.toString(id))));

        return productMono.map(product -> mapToResDto(product));
    }

    public Mono<ProductResDto> update(Long id, ProductReqDto productReqDto) {
        Mono<Product> productMono = repository
                .findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("product", "id", Long.toString(id))));

        return productMono
                .flatMap(product -> {
                    product.setName(productReqDto.getName());
                    product.setPrice(productReqDto.getPrice());
                    product.setCategory(productReqDto.getCategory());
                    product.setStockQuantity(productReqDto.getStockQuantity());
                    if (productReqDto.getDescription() != null && !productReqDto.getDescription().equals("")) {
                        product.setDescription(productReqDto.getDescription());
                    }
                    if (productReqDto.getImageUrl() != null && !productReqDto.getImageUrl().equals("")) {
                        product.setImageUrl(productReqDto.getImageUrl());
                    }
                    return repository.save(product);
                })
                .map(product -> mapToResDto(product));
    }

    public Mono<ProductResDto> updateQuantity(Long id, QuantityUpdateReqDto quantityUpdateReqDto) {
        Mono<Product> productMono = repository
                .findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("product", "id", Long.toString(id))));

        return productMono
                .flatMap(product -> {
                    if (quantityUpdateReqDto.getAction().toUpperCase().equals(Action.DEDUCT.toString())) {
                        if (product.getStockQuantity() >= quantityUpdateReqDto.getStockQuantity()) {
                            product.setStockQuantity(product.getStockQuantity() - quantityUpdateReqDto.getStockQuantity());
                        } else {
                            return Mono.error(new ResourceInsufficientException("product", "quantity"));
                        }
                    } else if (quantityUpdateReqDto.getAction().toUpperCase().equals(Action.ADD.toString())) {
                        product.setStockQuantity(product.getStockQuantity() + quantityUpdateReqDto.getStockQuantity());
                    }
                    return repository.save(product);
                })
                .map(product -> mapToResDto(product));
    }

    public Mono<Void> delete(Long id) {
        Mono<Product> productMono = repository
                .findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("product", "id", Long.toString(id))));

        return productMono.flatMap(product -> repository.deleteById(id));
    }

    private ProductResDto mapToResDto(Product product) {
        ProductResDto productResDto = new ProductResDto();
        productResDto.setId(product.getId());
        productResDto.setName(product.getName());
        productResDto.setPrice(product.getPrice());
        productResDto.setCategory(product.getCategory());
        productResDto.setStockQuantity(product.getStockQuantity());
        productResDto.setDescription(product.getDescription());
        productResDto.setImageUrl(product.getImageUrl());
        productResDto.setCreatedAt(product.getCreatedAt());
        productResDto.setUpdatedAt(product.getUpdatedAt());
        return productResDto;
    }

    private Product mapToEntity(ProductReqDto productReqDto) {
        Product product = new Product();
        product.setName(productReqDto.getName());
        product.setPrice(productReqDto.getPrice());
        product.setCategory(productReqDto.getCategory());
        product.setStockQuantity(productReqDto.getStockQuantity());
        product.setDescription(productReqDto.getDescription());
        product.setImageUrl(productReqDto.getImageUrl());
        return product;
    }

}
