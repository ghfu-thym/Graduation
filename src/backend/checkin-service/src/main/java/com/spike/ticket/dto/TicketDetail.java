package com.spike.ticket.dto;

public record TicketDetail(
        String ticketNumber,
        Long categoryId,
        String categoryName,
        String qrCode
) {
}