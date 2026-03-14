package com.spike.ticket.dto;

import lombok.Data;

import java.util.List;

@Data
public class ConfirmTicketRequest {
    private List<CategoryItem> categoryItemList;
    private String orderTrackingNumber;
}

