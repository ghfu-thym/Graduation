package com.spike.ticket.dto.event;

public record TicketCategoryDTO(
        Long eventId,
        String name,
        Long price,
        Integer quantity

) {
}
