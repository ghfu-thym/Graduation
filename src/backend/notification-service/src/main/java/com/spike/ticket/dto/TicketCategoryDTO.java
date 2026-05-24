package com.spike.ticket.dto;

public record TicketCategoryDTO(
        Long categoryId,
        Long eventId,
        String name,
        Long price,
        Integer quantity

) {
}
