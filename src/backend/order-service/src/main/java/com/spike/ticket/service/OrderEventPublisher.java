package com.spike.ticket.service;

import com.spike.ticket.dto.event.OrderConfirmedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public static final String TOPIC_ORDER_CONFIRMED = "order-confirmed-events";

    public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderConfirmed(String orderTrackingNumber, List<Long> ticketIds) {
        OrderConfirmedEvent event = new OrderConfirmedEvent(orderTrackingNumber, ticketIds);
        kafkaTemplate.send(TOPIC_ORDER_CONFIRMED, orderTrackingNumber, event);
        log.info("Order confirmed event published for order: {}, ticketIds: {}", orderTrackingNumber, ticketIds);
    }
}

