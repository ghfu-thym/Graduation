package com.spike.ticket.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ReserveTicketRequest {
    private Long eventID;
    private String ticketType;
    private int quantity;
    private List<Long> ticketIds;
}
