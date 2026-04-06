package com.spike.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TicketMetadata {
    private Long categoryId;
    private String categoryName;
    private Long price;
    private Long eventId;
    private String eventName;
    private String bannerUrl;
}
