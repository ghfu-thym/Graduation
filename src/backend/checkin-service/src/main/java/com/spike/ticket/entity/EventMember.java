package com.spike.ticket.entity;

import com.spike.ticket.enums.EventRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "event_members", indexes = {
        @Index(name = "idx_event_email", columnList = "event_id, email", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "emai", nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private EventRole role;
}
