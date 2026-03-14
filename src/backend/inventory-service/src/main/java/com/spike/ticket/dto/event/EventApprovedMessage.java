package com.spike.ticket.dto.event;

import java.util.List;

public record EventApprovedMessage(
        Long eventId,
        List<TicketCategoryDTO> ticketCategories
) {
}
