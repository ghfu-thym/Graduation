package com.spike.ticket.dto.event;

import com.spike.ticket.dto.CategoryItem;

import java.util.List;

public record OrderConfirmedEvent(
        String orderTrackingNumber,
        List<CategoryItem> categoryItems
) {
}

