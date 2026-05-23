package com.spike.ticket.entity;

import com.spike.ticket.enums.EventStatus;
import com.spike.ticket.utils.StringListConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "organizer_id", nullable = false)
    private Long organizerId;

    @Column(name = "organizer_name", nullable = false)
    private String organizerName;

    @Column(name = "organizer_email", nullable = false)
    private String organizerEmail;

    @Column(name = "location", nullable = false)
    private String location;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Column(name = "description")
    private String description;

    @Convert(converter = StringListConverter.class)
    @Column(name = "image_urls", columnDefinition = "json")
    private List<String> imageUrls;

    @Enumerated(EnumType.STRING)
    @Column
    private EventStatus status;

    @Column
    private LocalDateTime ticketOpenTime;

    @Column
    private Boolean isOpened;

    @Column
    private int shardCount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
