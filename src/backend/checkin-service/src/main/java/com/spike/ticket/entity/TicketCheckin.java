package com.spike.ticket.entity;

import com.spike.ticket.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ticket_checkins", indexes = {
        // Đánh Unique Index duy nhất cho ticket_number tối ưu tốc độ soát vé
        @Index(name = "idx_ticket_number", columnList = "ticket_number", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketCheckin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_number", nullable = false)
    private String ticketNumber;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TicketStatus status = TicketStatus.VALID;

}
