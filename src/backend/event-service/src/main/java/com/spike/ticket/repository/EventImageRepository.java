package com.spike.ticket.repository;

import com.spike.ticket.entity.EventImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventImageRepository extends JpaRepository<EventImage, Long> {
    @Query("select ei from EventImage ei where ei.event.eventId = :eventId order by ei.displayOrder asc")
    List<EventImage> findByEventIdOrderByDisplayOrderAsc(@Param("eventId") Long eventId);
}
