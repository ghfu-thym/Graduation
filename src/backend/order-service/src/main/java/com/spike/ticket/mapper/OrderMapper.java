package com.spike.ticket.mapper;

import com.spike.ticket.dto.respone.OrderResponse;
import com.spike.ticket.entity.Order;
import org.hibernate.annotations.Comment;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {
    public OrderResponse toOrderResponse(Order order) {

        if (order == null) return null;

        return OrderResponse.builder()
                .orderTrackingNumber(order.getOrderTrackingNumber())
                .status(order.getStatus().toString())
                .totalPrice(order.getTotalPrice())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
