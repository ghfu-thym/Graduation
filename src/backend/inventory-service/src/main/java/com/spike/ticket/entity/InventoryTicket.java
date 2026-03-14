/**
 * lưu số lượng vé trong kho
 * redis lấy data từ đây
 * thanh toán thành công thì trừ sô lượng ở đây
 */
package com.spike.ticket.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "inventory_tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long ticketCategoryId;

    @Column
    private String name;

    @Column
    private Integer totalQuantity;

    @Column
    private Integer availableQuantity;

    @Column
    private Long price;

}
