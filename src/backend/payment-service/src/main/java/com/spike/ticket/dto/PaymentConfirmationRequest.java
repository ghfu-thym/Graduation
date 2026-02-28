package com.spike.ticket.dto;

public record PaymentConfirmationRequest(
        String transactionId,
        String paymentMethod
) {
}
