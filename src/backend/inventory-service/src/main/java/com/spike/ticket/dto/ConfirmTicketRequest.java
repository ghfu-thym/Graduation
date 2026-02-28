package com.spike.ticket.dto;

import lombok.Data;

import java.util.List;

@Data
public class ConfirmTicketRequest {
    private List<Long> ticketIds;
    private String orderTrackingNumber;
}

