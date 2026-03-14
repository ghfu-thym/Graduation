package com.spike.ticket.dto;


import lombok.Data;

import java.util.List;

@Data
public class ReserveTicketRequest {

//    private Long eventID;
//
//    // Nếu không có chọn ghế
//    private String ticketType;
//    private int quantity;
//
//    // Nếu cần chọn ghế
//    private List<Long> categoryItems;
    private List<CategoryItem> categoryItemList;
}
