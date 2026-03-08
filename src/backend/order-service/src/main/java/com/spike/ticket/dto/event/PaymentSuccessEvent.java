package com.spike.ticket.dto.event;

public record PaymentSuccessEvent(
        String orderTrackingNumber,
        String transactionId,
        String status,
        Long amount
) {
}