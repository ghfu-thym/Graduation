package com.spike.ticket.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "ticket-service", url = "http://localhost:8081", primary = false)
public interface TicketClient {
    @PostMapping("/api/v1/tickets/reserve")
    Boolean reserveTicket(@RequestBody List<Long> ticketIds);
}
