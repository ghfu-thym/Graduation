package com.spike.ticket.dto;

public record TicketCreatedEvent(
        Long eventId,
        Long categoryId,
        String ticketNumber
) {
}