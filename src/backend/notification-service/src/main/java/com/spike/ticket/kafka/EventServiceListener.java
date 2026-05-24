package com.spike.ticket.kafka;


import com.spike.ticket.dto.EventApprovedInventoryMessage;
import com.spike.ticket.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventServiceListener {
    private final NotificationService notificationService;

    @KafkaListener(topics = "event-approved-inventory", groupId = "notification-group")
    public void handleEventApprovedInventory(EventApprovedInventoryMessage message) {
        log.info("[Kafka listener] Received event approved inventory message for eventId: {}", message.eventId());
        // lổ rồi
        try {
            notificationService.sendEventApprovedNotification(message.eventName(), message.organizerEmail(), message.organizerName());
        } catch (Exception e) {
            log.error("Error processing event approved inventory message for eventId: {}", message.eventId(), e);
            // TODO: dead letter queue
        }
    }
}
