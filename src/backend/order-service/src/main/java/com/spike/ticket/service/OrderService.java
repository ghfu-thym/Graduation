package com.spike.ticket.service;

import com.spike.ticket.dto.request.CreateOrderRequest;
import com.spike.ticket.dto.respone.OrderResponse;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrderByTrackingNumber(String trackingNumber);
}
