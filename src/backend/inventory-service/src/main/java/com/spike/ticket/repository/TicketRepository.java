package com.spike.ticket.repository;

import com.spike.ticket.dto.ConfirmTicketRequest;
import com.spike.ticket.dto.ReleaseTicketRequest;
import com.spike.ticket.entity.Ticket;
import com.spike.ticket.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Chỉ update nếu ghế đang AVAILABLE. Nếu ai đó đã đổi thành RESERVED rồi thì update này sẽ return 0 (thất bại).
//    @Modifying
//    @Query("UPDATE Ticket t SET t.status = :newStatus WHERE t.id = :ticketId AND t.status = :currentStatus")
//    int reserveSpecificTicket (@Param("ticketId") Long ticketId,
//                     @Param("currentStatus") TicketStatus currentStatus,
//                     @Param("newStatus") TicketStatus newStatus);
//
//
//    @Query(value = """
//        SELECT * FROM tickets
//        WHERE event_id = :eventId
//          AND ticket_type = :ticketType
//          AND status = 'AVAILABLE'
//        LIMIT 1
//        FOR UPDATE SKIP LOCKED
//        """, nativeQuery = true)
//    Optional<Ticket> findAndLockAvailableTicket(@Param("eventId") Long eventId,
//                                                @Param("ticketType") String ticketType);



}
