package com.spike.ticket.repository;

import com.spike.ticket.entity.EventImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventImageRepository extends JpaRepository<EventImage, Long> {
    List<EventImage> findByEventIdOrderByDisplayOrderAsc(Long eventId);
}

