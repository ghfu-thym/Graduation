package com.spike.ticket.kafka.publisher;

import com.spike.ticket.dto.event.OrderCancelledEvent;
import com.spike.ticket.dto.event.OrderConfirmedEvent;
import com.spike.ticket.dto.event.OrderRefundEvent;
import com.spike.ticket.dto.event.TicketCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public static final String TOPIC_ORDER_CONFIRMED = "order-confirmed-events";
    public static final String TOPIC_ORDER_CANCELLED = "order-cancelled-events";
    public static final String TOPIC_REFUND_ORDER = "refund-order-events";
    public static final String TOPIC_TICKET_CREATED = "ticket-created-events";

    public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderConfirmed(OrderConfirmedEvent event) {

        kafkaTemplate.send(TOPIC_ORDER_CONFIRMED, event.orderTrackingNumber(), event);
        log.info("[Kafka] Order confirmed event published for order: {}, categoryItems: {}", event.orderTrackingNumber(), event.categoryItems());
    }

    public void publishOrderCancelled(OrderCancelledEvent event) {

        kafkaTemplate.send(TOPIC_ORDER_CANCELLED, event.orderTrackingNumber(), event );
        log.info("[Kafka] Order cancelled event published for order: {}", event.orderTrackingNumber());
    }

    public void publishRefundOrder(String orderTrackingNumber, Long amount){
        OrderRefundEvent event = new OrderRefundEvent(orderTrackingNumber, amount);
        kafkaTemplate.send(TOPIC_REFUND_ORDER, orderTrackingNumber, event);
        log.info("[Kafka] Order refund event published for order: {}", orderTrackingNumber);
    }

    public void ticketCreated(TicketCreatedEvent event){

        kafkaTemplate.send(TOPIC_TICKET_CREATED, event.ticketNumber(), event);
        log.info("[Kafka] Ticket created event published for ticketId: {}", event.ticketNumber());
    }
}

