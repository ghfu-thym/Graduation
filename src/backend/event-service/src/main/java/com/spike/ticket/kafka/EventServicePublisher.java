package com.spike.ticket.kafka;

import com.spike.ticket.dto.EventApprovedInventoryMessage;
import com.spike.ticket.dto.EventApprovedMemberMessage;
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

    private static final String TOPIC_EVENT_APPROVED_INVENTORY = "event-approved-inventory";
    private static final String TOPIC_EVENT_APPROVED_MEMBER = "event-approved-member";
    private static final String TOPIC_INIT_INVENTORY = "init-inventory-events";

    public void publishEventApprovedInventory(Long eventId, List<TicketCategory> ticketCategories,
                                              String eventName, Long organizerId, String organizerName, String organizerEmail) {
        List<TicketCategoryDTO> dtos = ticketCategories.stream()
                .map(TicketCategoryDTO::fromEntity)
                .toList();

        EventApprovedInventoryMessage message = new EventApprovedInventoryMessage(eventId, eventName,
                organizerId, organizerName, organizerEmail, dtos);

        kafkaTemplate.send(TOPIC_EVENT_APPROVED_INVENTORY, eventId.toString(), message);

        log.info("[Kafka] Event approved message published for eventId: {}", eventId);
    }

    public void publishEventApprovedMember(Long eventId, List<Long> memberIds) {

        kafkaTemplate.send(TOPIC_EVENT_APPROVED_MEMBER, eventId.toString(), new EventApprovedMemberMessage(eventId, memberIds));

        log.info("[Kafka] Event approved member message published for eventId: {}", eventId);
    }

    public void publishInitInventory(Long eventId) {
        kafkaTemplate.send(TOPIC_INIT_INVENTORY, eventId.toString(), new InitInventoryMessage(eventId));
        log.info("[Kafka] Init inventory message published for eventId: {}", eventId);
    }
}
