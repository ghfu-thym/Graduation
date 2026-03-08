package com.spike.ticket.dto.event;

public record OrderRefundEvent(
        String orderTrackingNumber,
        Long amount
) {
}
