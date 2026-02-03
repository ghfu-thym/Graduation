package com.spike.ticket.controller;

import com.spike.ticket.dto.request.CreateOrderRequest;
import com.spike.ticket.dto.respone.OrderResponse;
import com.spike.ticket.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("Create order request for user: {}", request.getUserId());

        OrderResponse orderResponse = orderService.createOrder(request);

        return new ResponseEntity<>(orderResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{trackingNumber}")
    public ResponseEntity<OrderResponse> getOrderByTrackingNumber(@PathVariable String trackingNumber){
        log.info("Get order by tracking number: {}", trackingNumber);

        OrderResponse orderResponse = orderService.getOrderByTrackingNumber(trackingNumber);

        return ResponseEntity.ok(orderResponse);
    }
}
