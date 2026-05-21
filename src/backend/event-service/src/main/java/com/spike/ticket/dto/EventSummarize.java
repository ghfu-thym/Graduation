package com.spike.ticket.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventSummarize {
    private Long eventId;
    private String eventName;
    private String location;
    private String startTime;
    private Long minPrice;
    private String imageUrl;
}
