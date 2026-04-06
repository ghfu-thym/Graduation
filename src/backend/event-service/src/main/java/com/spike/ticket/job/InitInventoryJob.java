package com.spike.ticket.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spike.ticket.dto.TicketMetadata;
import com.spike.ticket.entity.Event;
import com.spike.ticket.entity.TicketCategory;
import com.spike.ticket.enums.EventStatus;
import com.spike.ticket.kafka.EventServicePublisher;
import com.spike.ticket.repository.EventRepository;
import com.spike.ticket.repository.TicketCategoryRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

//Polling Notification Pattern
@Slf4j
@Component
@RequiredArgsConstructor
public class InitInventoryJob {
    private final EventRepository eventRepository;
    private final EventServicePublisher eventServicePublisher;
    private final TicketCategoryRepo ticketCategoryRepo;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;

    // Chạy mỗi 1 phút (60000 ms)
    @Scheduled(fixedRate = 60000)
    public void scanAndInitInventory() {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime threshHold = now.plusMinutes(15);

        List<Event> upcomingEvents = eventRepository.findByTicketOpenTimeBetween(now, threshHold);

        // điều kiện:
        // 1. Đã được duyệt (PUBLISHED)
        // 2. Chưa được đẩy lên Redis (is_opened = false)
        for (Event event : upcomingEvents) {
            if (event.getStatus() == EventStatus.PUBLISHED && !event.getIsOpened()) {

                List<TicketCategory> ticketCategories = ticketCategoryRepo.findByEventId(event.getId());

                for (TicketCategory ticketCategory : ticketCategories) {
                    TicketMetadata metadata = new TicketMetadata(
                            ticketCategory.getId(),
                            ticketCategory.getName(),
                            ticketCategory.getPrice(),
                            ticketCategory.getEventId(),
                            event.getName(),
                            event.getImageUrl()
                    );

                    String json = "";

                    try {
                        json = objectMapper.writeValueAsString(metadata);
                        log.info("Ticket metadata json: {}", json);
                    } catch (JsonProcessingException e) {
                        log.error("Cannot serialize TicketMetadata for categoryId={}", ticketCategory.getId(), e);
                    }

                    String redisKey = "ticket:category:" + ticketCategory.getId() + ":metadata";
                    long ttl = Duration.between(LocalDateTime.now(), event.getEndTime().plusDays(1)).getSeconds();
                    redisTemplate.opsForValue().set(redisKey, json, ttl, TimeUnit.SECONDS);
                }

                eventServicePublisher.publishInitInventory(event.getId());
                event.setIsOpened(true);
                eventRepository.save(event);
            }
        }
    }
}
