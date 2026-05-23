package com.spike.ticket.client;

import com.spike.ticket.dto.request.PaymentRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service", url = "http://localhost:8082")
public interface PaymentClient {
    @PostMapping("/api/v1/payments/create-url")
    ResponseEntity<String> createPaymentUrl(@RequestBody PaymentRequest request);
}
