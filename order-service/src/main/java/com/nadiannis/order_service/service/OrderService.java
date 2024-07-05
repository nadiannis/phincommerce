package com.nadiannis.order_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nadiannis.common.dto.MessageDto;
import com.nadiannis.order_service.dto.*;
import com.nadiannis.common.dto.OrderResDto;
import com.nadiannis.common.dto.OrderItemResDto;
import com.nadiannis.order_service.entity.Order;
import com.nadiannis.order_service.entity.OrderItem;
import com.nadiannis.common.exception.ResourceNotFoundException;
import com.nadiannis.order_service.repository.OrderItemRepository;
import com.nadiannis.order_service.repository.OrderRepository;
import com.nadiannis.order_service.utils.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private OrderRepository orderRepository;

    private OrderItemRepository orderItemRepository;

    private KafkaTemplate<String, Object> kafkaTemplate;

    private ObjectMapper objectMapper;

    @Autowired
    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public Flux<OrderResDto> getAll() {
        return orderRepository.findAll().flatMap(order -> {
            return orderItemRepository.findByOrderId(order.getId())
                    .collectList()
                    .map(orderItems -> {
                        OrderResDto orderResDto = mapToOrderResDto(order);
                        orderResDto.setOrderItems(orderItems.stream().map(this::mapToOrderItemResDto).collect(Collectors.toList()));
                        return orderResDto;
                    });
        });
    }

//    @KafkaListener(topics = "orchestrator", groupId = "phincommerce")
//    public void handleStatusUpdate(String message) throws JsonProcessingException {
//        MessageDto messageDto = objectMapper.readValue(message, MessageDto.class);
//
//        if (messageDto.getStatus().equals("PRODUCT_DEDUCTED_FAILED")) {
//            System.out.println("PRODUCT_DEDUCTED_FAILED (complete the order):" + messageDto);
//        } else if (messageDto.getStatus().equals("PAYMENT_APPROVED")) {
//            System.out.println("PAYMENT_APPROVED (complete the order):" + messageDto);
//        } else if (messageDto.getStatus().equals("PAYMENT_REJECTED")) {
//            System.out.println("PAYMENT_REJECTED (complete the order):" + messageDto);
//        }
//    }

    private void completeOrder(MessageDto messageDto, String status) {
        Long orderId = messageDto.getPayload().getId();
        StatusUpdateReqDto statusUpdateReqDto = StatusUpdateReqDto.builder().orderStatus(status).build();

        updateStatus(orderId, statusUpdateReqDto).subscribe(
                orderResDto -> {
                    System.out.println("Order status updated successfully to " + status);
                },
                error -> {
                    System.out.println("Failed to update order status: " + error.getMessage());
                }
        );
    }

    public Mono<OrderResDto> add(OrderReqDto orderReqDto) {
        if (orderReqDto.getOrderItems() == null) {
            orderReqDto.setOrderItems(Collections.emptyList());
        }
        Float totalAmount = (float) orderReqDto.getOrderItems().stream()
                .mapToDouble(orderItemReqDto -> orderItemReqDto.getPrice() * orderItemReqDto.getQuantity())
                .sum();

        Order order = mapToOrderEntity(orderReqDto);
        order.setOrderStatus(OrderStatus.CREATED.toString());
        order.setTotalAmount(totalAmount);

        return orderRepository.save(order).flatMap(newOrder -> {
            List<OrderItem> orderItems = orderReqDto.getOrderItems().stream().map(orderItemReqDto -> {
                OrderItem orderItem = mapToOrderItemEntity(orderItemReqDto);
                orderItem.setOrderId(newOrder.getId());
                return orderItem;
            }).collect(Collectors.toList());

            return orderItemRepository.saveAll(orderItems).collectList().map(newOrderItems -> {
                OrderResDto createdOrderResDto = mapToOrderResDto(newOrder);
                createdOrderResDto.setOrderItems(newOrderItems.stream()
                        .map(newOrderItem -> mapToOrderItemResDto(newOrderItem))
                        .collect(Collectors.toList()));

                kafkaTemplate.send(
                        "orchestrator",
                        MessageDto.builder()
                                .status("ORDER_CREATED")
                                .payload(createdOrderResDto)
                                .build()
                );

                return createdOrderResDto;
            });
        });
    }

    public Mono<OrderResDto> getById(Long id) {
        Mono<Order> orderMono = orderRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("order", "id", Long.toString(id))));

        return orderMono.flatMap(order -> {
            return orderItemRepository.findByOrderId(order.getId())
                    .collectList()
                    .map(orderItems -> {
                        OrderResDto orderResDto = mapToOrderResDto(order);
                        orderResDto.setOrderItems(orderItems.stream().map(this::mapToOrderItemResDto).collect(Collectors.toList()));
                        return orderResDto;
                    });
        });
    }

    public Mono<OrderResDto> updateStatus(Long id, StatusUpdateReqDto statusUpdateReqDto) {
        Mono<Order> orderMono = orderRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("order", "id", Long.toString(id))));

        return orderMono
                .flatMap(order -> {
                    order.setOrderStatus(statusUpdateReqDto.getOrderStatus().toUpperCase());
                    return orderRepository.save(order);
                }).flatMap(order -> {
                    return orderItemRepository.findByOrderId(order.getId())
                            .collectList()
                            .map(orderItems -> {
                                OrderResDto orderResDto = mapToOrderResDto(order);
                                orderResDto.setOrderItems(orderItems.stream().map(this::mapToOrderItemResDto).collect(Collectors.toList()));
                                return orderResDto;
                            });
                });
    }

    public Mono<Void> delete(Long id) {
        Mono<Order> orderMono = orderRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("order", "id", Long.toString(id))));

        return orderMono.flatMap(order -> orderRepository.deleteById(order.getId()));
    }

    private OrderResDto mapToOrderResDto(Order order) {
        OrderResDto orderResDto = new OrderResDto();
        orderResDto.setId(order.getId());
        orderResDto.setPaymentMethod(order.getPaymentMethod());
        orderResDto.setBillingAddress(order.getBillingAddress());
        orderResDto.setShippingAddress(order.getShippingAddress());
        orderResDto.setOrderStatus(order.getOrderStatus());
        orderResDto.setTotalAmount(order.getTotalAmount());
        orderResDto.setCustomerId(order.getCustomerId());
        orderResDto.setOrderDate(order.getOrderDate());
        return orderResDto;
    }

    private Order mapToOrderEntity(OrderReqDto orderReqDto) {
        Order order = new Order();
        order.setPaymentMethod(orderReqDto.getPaymentMethod().toUpperCase());
        order.setBillingAddress(orderReqDto.getBillingAddress());
        order.setShippingAddress(orderReqDto.getShippingAddress());
        order.setCustomerId(orderReqDto.getCustomerId());
        return order;
    }

    private OrderItemResDto mapToOrderItemResDto(OrderItem orderItem) {
        OrderItemResDto orderItemResDto = new OrderItemResDto();
        orderItemResDto.setId(orderItem.getId());
        orderItemResDto.setOrderId(orderItem.getOrderId());
        orderItemResDto.setProductId(orderItem.getProductId());
        orderItemResDto.setPrice(orderItem.getPrice());
        orderItemResDto.setQuantity(orderItem.getQuantity());
        return orderItemResDto;
    }

    private OrderItem mapToOrderItemEntity(OrderItemReqDto orderItemReqDto) {
        OrderItem orderItem = new OrderItem();
        orderItem.setProductId(orderItemReqDto.getProductId());
        orderItem.setPrice(orderItemReqDto.getPrice());
        orderItem.setQuantity(orderItemReqDto.getQuantity());
        return orderItem;
    }

}
