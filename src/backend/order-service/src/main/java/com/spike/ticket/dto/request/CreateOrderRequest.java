package com.spike.ticket.dto.request;

import com.spike.ticket.dto.event.CategoryItem;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    @NotNull(message = "eventId cannot be null")
    private Long eventId;

    List<CategoryItem> categoryItems;

}
