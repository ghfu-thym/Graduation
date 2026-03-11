package com.spike.ticket.repository;

import com.spike.ticket.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
    Event findByEventId(Long eventId);
}
