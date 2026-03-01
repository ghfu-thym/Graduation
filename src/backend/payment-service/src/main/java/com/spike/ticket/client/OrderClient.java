package com.spike.ticket.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "order-service", url = "http://localhost:8080")
public interface OrderClient {
    @PostMapping("/api/v1/orders/{orderTrackingNumber}/confirm-payment")
    void confirmOrderPaid(@PathVariable String orderTrackingNumber);
}
