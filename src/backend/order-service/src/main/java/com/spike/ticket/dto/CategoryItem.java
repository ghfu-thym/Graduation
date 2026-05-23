package com.spike.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CategoryItem {
    private Long ticketCategoryId;
    private int quantity;
}
