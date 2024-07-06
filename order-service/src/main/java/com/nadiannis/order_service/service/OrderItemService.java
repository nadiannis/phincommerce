package com.nadiannis.order_service.service;

import com.nadiannis.common.dto.order.OrderItemResDto;
import com.nadiannis.order_service.entity.OrderItem;
import com.nadiannis.order_service.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class OrderItemService {

    private OrderItemRepository repository;

    @Autowired
    public OrderItemService(OrderItemRepository repository) {
        this.repository = repository;
    }

    public Flux<OrderItemResDto> getAll() {
        return repository.findAll().map(orderItem -> mapToOrderItemResDto(orderItem));
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

}
