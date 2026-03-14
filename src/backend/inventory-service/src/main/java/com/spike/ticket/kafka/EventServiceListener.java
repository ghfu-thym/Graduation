package com.spike.ticket.kafka;

import com.spike.ticket.dto.event.EventApprovedMessage;
import com.spike.ticket.dto.event.InitInventoryMessage;
import com.spike.ticket.repository.TicketCategoryRepo;
import com.spike.ticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventServiceListener {
    private final TicketService ticketService;
    private final TicketCategoryRepo ticketCategoryRepo;

    @KafkaListener(topics = "event-approved-events", groupId = "ticket-group")
    public void handleEventApproved (EventApprovedMessage message) {
        log.info("[Kafka listener] Received event approved message: {}", message);

        try {
            ticketService.handleEventApproved(message);

        } catch (Exception e) {
            log.error("[Kafka listener] Error processing event approved message: {}", e.getMessage());

            //TODO: dead letter queue
        }
    }

    @KafkaListener(topics = "init-inventory-events", groupId = "ticket-group")
    public void handleInitInventory (InitInventoryMessage message) {
        log.info("[Kafka listener] Received init inventory message: {}", message);

        try {
            ticketService.initTicket(message.eventId());
        } catch (Exception e) {
            log.error("[Kafka listener] Error processing init inventory message: {}", e.getMessage());
            throw new RuntimeException(e);

            //TODO: dead letter queue
        }
    }
}
