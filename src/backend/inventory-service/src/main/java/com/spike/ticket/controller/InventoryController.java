package com.spike.ticket.controller;

import com.spike.ticket.dto.ConfirmTicketRequest;
import com.spike.ticket.dto.ReleaseTicketRequest;
import com.spike.ticket.dto.ReserveTicketRequest;
import com.spike.ticket.dto.TicketReservationResponse;
import com.spike.ticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/tickets")
@RequiredArgsConstructor
public class InventoryController {
    private final TicketService ticketService;

    @PostMapping("/reserve")
    public ResponseEntity<TicketReservationResponse> reserveTickets(@RequestBody ReserveTicketRequest request){
        try {

            return ResponseEntity.ok(ticketService.reserveTicket(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }

    @PostMapping("/release")
    public ResponseEntity<?> releaseTickets(@RequestBody ReleaseTicketRequest request){
        ticketService.releaseTickets(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmTickets(@RequestBody ConfirmTicketRequest request){
        ticketService.confirmTickets(request);
        return ResponseEntity.ok().build();
    }
}
