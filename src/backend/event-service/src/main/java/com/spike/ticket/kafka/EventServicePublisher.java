package com.spike.ticket.kafka;

import com.spike.ticket.dto.EventApprovedMessage;
import com.spike.ticket.dto.InitInventoryMessage;
import com.spike.ticket.dto.TicketCategoryDTO;
import com.spike.ticket.entity.TicketCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class EventServicePublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public EventServicePublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    private static final String TOPIC_EVENT_APPROVED = "event-approved-events";
    private static final String TOPIC_INIT_INVENTORY = "init-inventory-events";

    public void publishEventApproved(Long eventId, List<TicketCategory> ticketCategories) {
        List<TicketCategoryDTO> dtos = ticketCategories.stream()
                .map(TicketCategoryDTO::fromEntity)
                .toList();

        EventApprovedMessage message = new EventApprovedMessage(eventId, dtos);

        kafkaTemplate.send(TOPIC_EVENT_APPROVED, eventId.toString(), message);

        log.info("[Kafka] Event approved message published for eventId: {}", eventId);
    }

    public void publishInitInventory(Long eventId){
        kafkaTemplate.send(TOPIC_INIT_INVENTORY, eventId.toString(), new InitInventoryMessage(eventId));
        log.info("[Kafka] Init inventory message published for eventId: {}", eventId);
    }
}
