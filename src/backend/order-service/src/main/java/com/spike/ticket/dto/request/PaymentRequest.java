package com.spike.ticket.dto.request;

public record PaymentRequest(
        String orderTrackingNumber,
        Long amount
) {
}
