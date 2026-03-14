package com.spike.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseTicketRequest {
    private List<CategoryItem> categoryItems;

    private String orderTrackingNumber;
}
