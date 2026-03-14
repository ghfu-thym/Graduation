/**
 * lưu thông tin vé từ message
 */
package com.spike.ticket.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ticket_categories")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicketCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private Long price;

    @Column
    private Integer quantity;

    @Column
    private Long eventId;
}
