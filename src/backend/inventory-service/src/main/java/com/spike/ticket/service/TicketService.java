package com.spike.ticket.service;


import com.spike.ticket.dto.ReserveTicketRequest;
import com.spike.ticket.entity.Ticket;
import com.spike.ticket.enums.TicketStatus;
import com.spike.ticket.repository.TicketRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {
    private final TicketRepository ticketRepository;

    @Transactional
    public List<Long> reserveTickets(ReserveTicketRequest request) {
        List<Long> reservedTicketIds = new ArrayList<>();
        if(request.getTicketIds() != null && !request.getTicketIds().isEmpty()) {
            log.info("Reserve tickets in seats: {}", request.getTicketIds());
            for (Long ticketId : request.getTicketIds()) {
                int updateRow = ticketRepository.reserveSpecificTicket(
                        ticketId,
                        TicketStatus.AVAILABLE,
                        TicketStatus.RESERVED
                );
                if (updateRow == 0) {
                    throw new RuntimeException("Ticket ID: " + ticketId + " is already reserved!");
                }

                reservedTicketIds.add(ticketId);
            }
        } else if (request.getQuantity()>0) {
            log.info("Reserve tickets no seat: {}", request.getQuantity());
            for (int i = 0; i < request.getQuantity(); i++) {
                Ticket ticket = ticketRepository.findAndLockAvailableTicket(
                        request.getEventID(),request.getTicketType()
                ).orElseThrow(() -> new RuntimeException(
                        "No available ticket for event ID: " + request.getEventID() + " with ticket type: " + request.getTicketType()));
                ticket.setStatus(TicketStatus.RESERVED);
                ticketRepository.save(ticket);
                reservedTicketIds.add(ticket.getId());
            }
        } else {
            throw new IllegalArgumentException("Invalid request!");
        }
        return reservedTicketIds;
    }
}
