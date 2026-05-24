package com.spike.ticket.dto;

import java.util.List;

public record TicketCreatedEvent(
        Long eventId,
        String orderTrackingNumber,
        String eventName,
        String username,
        String email,
        List<TicketDetail> tickets
) {
}