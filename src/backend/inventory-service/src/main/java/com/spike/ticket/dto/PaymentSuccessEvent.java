package com.spike.ticket.dto;

public record PaymentSuccessEvent(
        String orderTrackingNumber,
        String transactionId,
        String status,
        Long amount
) {
}
