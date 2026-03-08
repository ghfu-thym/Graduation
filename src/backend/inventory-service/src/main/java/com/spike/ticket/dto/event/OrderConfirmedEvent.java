package com.spike.ticket.dto.event;

import java.util.List;

public record OrderConfirmedEvent(
        String orderTrackingNumber,
        List<Long> ticketIds
) {
}

