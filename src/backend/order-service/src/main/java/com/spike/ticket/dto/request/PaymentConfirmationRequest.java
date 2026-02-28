package com.spike.ticket.dto.request;

// chỉ nhận thông tin, không cần sửa nên để là record
public record PaymentConfirmationRequest (
        String transactionId,
        String paymentMethod
) {

}
