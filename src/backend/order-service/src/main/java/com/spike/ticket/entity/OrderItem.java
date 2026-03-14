package com.spike.ticket.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "order_items", indexes = {
        @Index(name = "idx_order_id", columnList = "order_id"),
})
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Chỉ query tới order khi thực sự gọi item.getOrder, tránh query không cần thiết
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column
    private String name;

    @Column(name = "price_per_ticket", nullable = false, precision = 15, scale = 2)
    private Long pricePerTicket;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

}
