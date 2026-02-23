package com.spike.ticket.dto.respone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicketReservationResponse {
    private Long ticketId;
    private BigDecimal price;    // Giá vé
}
