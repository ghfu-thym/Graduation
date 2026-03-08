package com.spike.ticket.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    @NotNull(message = "userId cannot be null")
    private Long userId;

    @NotNull(message = "eventId cannot be null")
    private Long eventId;

    // Nếu không chọn ghế
    private String ticketType;
    private int quantity;

    //Nếu có chọn ghế
    private List<Long> ticketIds;

    @NotNull
    private Long totalPrice;
}
