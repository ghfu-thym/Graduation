package com.spike.ticket.dto.request;

import com.spike.ticket.dto.CategoryItem;
import lombok.Data;

import java.util.List;

@Data
public class ReserveTicketRequest {
    private Long eventID;

    private List<CategoryItem> categoryItemList;
}
