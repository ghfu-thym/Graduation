package com.spike.ticket.repository;

import com.spike.ticket.entity.TicketCheckin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketCheckinRepository extends JpaRepository<TicketCheckin, Long> {
    TicketCheckin findByTicketNumber(String ticketNumber);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE TicketCheckin t " +
            "SET t.status = 'SCANNED' " +
            "WHERE t.ticketNumber = :ticketNumber " +
            "  AND t.eventId = :eventId " +
            "  AND t.status = 'VALID'")
    int scanAndLockTicket(@Param("ticketNumber") String ticketNumber, @Param("eventId") Long eventId);
}
