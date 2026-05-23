package com.spike.ticket.dto;

import com.spike.ticket.entity.TicketCategory;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class EventDetail {
    private Long id;
    private String name;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;
    private List<String> imageUrls;
    List<TicketCategory> categoryItemList;
    private int shardCount;
    private LocalDateTime createdAt;
}
