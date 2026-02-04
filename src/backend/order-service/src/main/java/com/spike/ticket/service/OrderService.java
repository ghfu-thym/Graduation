package com.spike.ticket.service;

import com.spike.ticket.dto.request.CreateOrderRequest;
import com.spike.ticket.dto.respone.OrderResponse;
import org.springframework.data.domain.Page;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrderByTrackingNumber(String trackingNumber);

    OrderResponse cancelOrder(String orderTrackingNumber);

    Page<OrderResponse> getOrdersByUserID(Long userID, int page, int size);

    OrderResponse completePayment(String orderTrackingNumber);
}
