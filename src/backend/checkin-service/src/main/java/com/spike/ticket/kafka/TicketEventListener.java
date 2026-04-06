package com.spike.ticket.kafka;

import com.spike.ticket.dto.TicketCreatedEvent;
import com.spike.ticket.service.CheckinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketEventListener {

    private final CheckinService checkinService;

    @KafkaListener(topics = "ticket-created-events", groupId = "checkin-group")
    public void handleTicketCreatedEvent(TicketCreatedEvent event) {

        log.info("[Kafka listener] Received ticket created event for ticketId: {}", event.ticketNumber());

        try {
            checkinService.createTicketCheckin(event);
        } catch (Exception e) {
            log.error("[Kafka listener] Error processing ticket created event: {}", e.getMessage());
            //TODO: dead letter queue
        }
    }
}
