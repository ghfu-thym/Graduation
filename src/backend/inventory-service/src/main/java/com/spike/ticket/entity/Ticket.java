package com.spike.ticket.entity;


import com.spike.ticket.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tickets")
@Data
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String seatNumber;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    @Version
    private Integer version;
}
