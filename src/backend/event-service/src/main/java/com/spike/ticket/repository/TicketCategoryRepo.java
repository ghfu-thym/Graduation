package com.spike.ticket.repository;

import com.spike.ticket.entity.TicketCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketCategoryRepo extends JpaRepository<TicketCategory, Long> {

    void deleteByEventId(Long eventId);

    List<TicketCategory> findByEventId(Long eventId);
}
