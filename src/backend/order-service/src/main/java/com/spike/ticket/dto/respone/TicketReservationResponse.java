package com.spike.ticket.dto.respone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketReservationResponse {
    private boolean success;
    private Long failedCategoryIndex;
}