package com.spike.ticket.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * chỉ sinh ra sau khi thanh toán thành công
 */

@Entity
@Table(name = "tickets")
@Data
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ticketNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Long ticketCategoryId;

    @Column(nullable = false, unique = true)
    private String qrCode;
}
