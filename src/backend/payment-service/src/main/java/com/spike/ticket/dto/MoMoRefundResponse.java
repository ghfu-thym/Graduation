package com.spike.ticket.dto;

public record MoMoRefundResponse(
        String partnerCode,
        String orderId,
        String requestId,
        Long amount,
        Long transId,
        Integer resultCode,  // Trạng thái: 0 là thành công
        String message,
        Long responseTime
) {}