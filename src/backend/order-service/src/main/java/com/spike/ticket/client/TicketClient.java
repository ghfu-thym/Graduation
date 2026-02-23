package com.spike.ticket.client;

import com.spike.ticket.dto.request.ReserveTicketRequest;
import com.spike.ticket.dto.respone.TicketReservationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "ticket-service", url = "http://localhost:8081")
public interface TicketClient {
    @PostMapping("/api/v1/tickets/reserve")
    List<TicketReservationResponse> reserveTicket(@RequestBody ReserveTicketRequest request);
}
