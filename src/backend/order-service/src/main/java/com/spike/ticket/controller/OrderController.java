package com.spike.ticket.controller;

import com.spike.ticket.dto.request.CreateOrderRequest;
import com.spike.ticket.dto.request.PaymentConfirmationRequest;
import com.spike.ticket.dto.respone.OrderResponse;
import com.spike.ticket.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            JwtAuthenticationToken auth) {
        Long userId = Long.parseLong(auth.getToken().getSubject());
        log.info("Create order request for user: {}", userId);

        OrderResponse orderResponse = orderService.createOrder(request, userId);

        return new ResponseEntity<>(orderResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{trackingNumber}")
    public ResponseEntity<OrderResponse> getOrderByTrackingNumber(@PathVariable String trackingNumber){
        log.info("Get order by tracking number: {}", trackingNumber);

        OrderResponse orderResponse = orderService.getOrderByTrackingNumber(trackingNumber);

        return ResponseEntity.ok(orderResponse);
    }

    @PutMapping("/{orderTrackingNumber}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable String orderTrackingNumber){
        log.info("Cancel order with ID: {}", orderTrackingNumber);

        OrderResponse orderResponse = orderService.cancelOrder(orderTrackingNumber);

        return ResponseEntity.ok(orderResponse);
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getOrdersByUserID(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            JwtAuthenticationToken auth
    ){
        Long userId = Long.parseLong(auth.getToken().getSubject());
        log.info("Get orders by user ID: {}", userId);

        Page<OrderResponse> response = orderService.getOrdersByUserID(userId, page, size);

        if (response.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return ResponseEntity.ok(response);
    }

    // giữ lại nếu sau này kafka chết thì gọi thẳng
    @PutMapping("/{orderTrackingNumber}/confirm-payment")
    public ResponseEntity<Void> confirmOrderPaid(
            @PathVariable String orderTrackingNumber,
            @RequestBody PaymentConfirmationRequest request){

        log.info("Pay order with ID: {}", orderTrackingNumber);
        orderService.completePayment(orderTrackingNumber, request.transactionId());
        return ResponseEntity.ok().build();
    }
}
