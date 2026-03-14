package com.spike.ticket.dto;

import java.util.List;

public record EventApprovedMessage(
        Long eventId,
        List<TicketCategoryDTO> ticketCategories
) {
}
