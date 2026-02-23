package com.spike.ticket.repository;

import com.spike.ticket.entity.Ticket;
import com.spike.ticket.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Chỉ update nếu ghế đang AVAILABLE. Nếu ai đó đã đổi thành RESERVED rồi thì update này sẽ return 0 (thất bại).
    @Modifying
    @Query("UPDATE Ticket t SET t.status = :newStatus WHERE t.id = :ticketId AND t.status = :currentStatus")
    int updateStatus(@Param("seatId") Long seatId,
                     @Param("currentStatus") TicketStatus currentStatus,
                     @Param("newStatus") TicketStatus newStatus);
}
