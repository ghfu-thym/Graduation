package com.spike.ticket.repository;

import com.spike.ticket.entity.TicketCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketCategoryRepo extends JpaRepository<TicketCategory, Long> {

    void deleteByEventId(Long eventId);

    List<TicketCategory> findByEventId(Long eventId);

    @Query("select min(tc.price) from TicketCategory tc where tc.eventId = :eventId")
    Long findMinPriceByEventId(@Param("eventId") Long eventId);

}
