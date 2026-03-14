package com.spike.ticket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

/** trong trường hợp gửi 2 ConfirmTickerRequest,
 * sẽ check orderTrackingNumber để kiểm tra xem đã trừ kho chưa
 * tránh trừ số lượng 2 lần
 **/
@Entity
@Table(name = "processed_orders")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String orderTrackingNumber;
}
