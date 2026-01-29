package com.spike.ticket.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "order_items", indexes = {
        @Index(name = "idx_order_id", columnList = "order_id"),
        @Index(name = "idx_ticket_id", columnList = "ticket_id")
})
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Chỉ query tới order khi thực sự gọi item.getOrder, tránh query không cần thiết
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "snapshot_event", nullable = false)
    private String snapshotEvent;

    @Column(name = "snapshot_seat", nullable = false, length = 100)
    private String snapshotSeat;
}
