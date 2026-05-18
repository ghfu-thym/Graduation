package com.spike.ticket.dto;

import java.util.List;

public record EventApprovedInventoryMessage(
        Long eventId,
        String eventName,
        Long organizationId,
        String organizationName,
        String organizerEmail,
        List<TicketCategoryDTO> ticketCategories
) {
}
