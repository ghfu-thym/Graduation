package com.spike.ticket.repository;

import com.spike.ticket.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    Event findByEventId(Long eventId);

    List<Event> findByTicketOpenTimeBetween(LocalDateTime from, LocalDateTime to);
}
