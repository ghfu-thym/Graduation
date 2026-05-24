package com.spike.ticket.kafka;

import com.spike.ticket.dto.TicketCreatedEvent;
import com.spike.ticket.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderServiceListener {
    private final NotificationService notificationService;

    @KafkaListener(topics = "ticket-created-events", groupId = "notification-group")
    public void handleTicketCreated(TicketCreatedEvent event) {
        log.info("[Kafka listener] Received ticket created event for order: {}", event.orderTrackingNumber());
        // lổ rồi
        notificationService.sendTicketByOrder(event);
    }
}
