package com.spike.ticket.dto;

import com.spike.ticket.entity.TicketCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateTicketRequest {
    @Valid
    @NotEmpty(message = "List ticket cannot empty")
    private List<TicketCategory> ticketCategories;
}
