package com.spike.ticket.kafka;

import com.spike.ticket.dto.EventApprovedMemberMessage;
import com.spike.ticket.service.CheckinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventMemberListener {
    private final CheckinService checkinService;

    @KafkaListener(topics = "event-approved-member", groupId = "checkin-group")
    public void handleEventApproved (EventApprovedMemberMessage message) {
        log.info("[Kafka listener] Received event approved member message: {}", message);

        try {
            checkinService.createEventMember(message);
        } catch (Exception e) {
            log.error("[Kafka listener] Error processing event approved member message: {}", e.getMessage());
            //TODO: dead letter queue
        }
    }
}
