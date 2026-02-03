package com.spike.ticket.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderRequest {
    @NotNull(message = "userId cannot be null")
    private Long userId;

    @NotNull(message = "must select at least 1 ticket")
    private List<Long> seatIds;

    @NotNull
    private BigDecimal totalPrice;
}
