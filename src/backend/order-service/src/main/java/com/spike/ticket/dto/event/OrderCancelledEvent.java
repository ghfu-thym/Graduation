package com.spike.ticket.dto.event;

import java.util.List;

public record OrderCancelledEvent(
        String orderTrackingNumber,
        List<CategoryItem> categoryItems
) {
}
