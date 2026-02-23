package com.spike.ticket.controller;

import com.spike.ticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/tickets")
@RequiredArgsConstructor
public class InventoryController {
    private final TicketService ticketService;

    @PostMapping("/reserve")
    public ResponseEntity<Boolean> reserveTickets(@RequestBody List<Long> ticketIds){
        try {
            ticketService.reserveTicket(ticketIds);
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(false);
        }
    }
}
