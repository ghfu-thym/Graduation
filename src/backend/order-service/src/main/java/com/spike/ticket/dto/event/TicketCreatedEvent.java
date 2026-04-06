package com.spike.ticket.dto.event;

public record TicketCreatedEvent(
        Long eventId,
        Long categoryId,
        String ticketNumber
) {
}
