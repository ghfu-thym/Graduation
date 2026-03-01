package com.spike.ticket.dto;

public record PaymentRequest(
        String orderTrackingNumber,
        Long amount
) {
}
