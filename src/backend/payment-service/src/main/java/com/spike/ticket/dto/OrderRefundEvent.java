package com.spike.ticket.dto;

public record OrderRefundEvent(
        String orderTrackingNumber,
        Long amount
) {
}

