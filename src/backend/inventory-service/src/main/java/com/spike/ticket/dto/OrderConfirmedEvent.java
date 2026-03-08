package com.spike.ticket.dto;

import java.util.List;

public record OrderConfirmedEvent(
        String orderTrackingNumber,
        List<Long> ticketIds
) {
}

