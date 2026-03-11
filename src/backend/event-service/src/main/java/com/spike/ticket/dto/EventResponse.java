package com.spike.ticket.dto;

import com.spike.ticket.entity.Event;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class EventResponse {
    private Long id;
    private String name;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;
    private List<String> imageUrls;
    private LocalDateTime createdAt;

    public static EventResponse fromEntity(Event event, List<String> imageUrls) {
        return EventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .location(event.getLocation())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .description(event.getDescription())
                .imageUrls(imageUrls)
                .createdAt(event.getCreatedAt())
                .build();
    }
}

