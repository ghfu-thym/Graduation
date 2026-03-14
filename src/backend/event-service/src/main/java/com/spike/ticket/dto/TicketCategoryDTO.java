package com.spike.ticket.dto;

import com.spike.ticket.entity.TicketCategory;

public record TicketCategoryDTO(
        Long eventId,
        String name,
        Long price,
        Integer quantity

) {
    public static TicketCategoryDTO fromEntity(TicketCategory entity) {
        return new TicketCategoryDTO(
                entity.getEventId(),
                entity.getName(),
                entity.getPrice(),
                entity.getQuantity()
        );
    }
}

