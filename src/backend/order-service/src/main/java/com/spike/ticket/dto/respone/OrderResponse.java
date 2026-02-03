package com.spike.ticket.dto.respone;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderResponse {
    // id bên ngoài chỉ dùng tracking number, id đang là auto incre dễ lộ
    private String orderTrackingNumber;
    private String status;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
}
