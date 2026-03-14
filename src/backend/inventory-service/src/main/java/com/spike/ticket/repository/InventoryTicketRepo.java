package com.spike.ticket.repository;

import com.spike.ticket.entity.InventoryTicket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryTicketRepo extends JpaRepository<InventoryTicket, Long> {
    InventoryTicket findByTicketId(Long ticketId);

    // trừ số lượng availableQuantity
    Long decrementQuantityByTicketId(Long ticketId);
}
