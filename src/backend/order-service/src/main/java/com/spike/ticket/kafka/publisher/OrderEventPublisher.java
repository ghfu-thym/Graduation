package com.spike.ticket.kafka.publisher;

import com.spike.ticket.dto.event.OrderCancelledEvent;
import com.spike.ticket.dto.event.OrderConfirmedEvent;
import com.spike.ticket.dto.event.OrderRefundEvent;
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

    public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderConfirmed(String orderTrackingNumber, List<Long> ticketIds) {
        OrderConfirmedEvent event = new OrderConfirmedEvent(orderTrackingNumber, ticketIds);
        kafkaTemplate.send(TOPIC_ORDER_CONFIRMED, orderTrackingNumber, event);
        log.info("[Kafka] Order confirmed event published for order: {}, ticketIds: {}", orderTrackingNumber, ticketIds);
    }

    public void publishOrderCancelled(String orderTrackingNumber, List<Long> ticketIds) {
        OrderCancelledEvent event = new OrderCancelledEvent(orderTrackingNumber, ticketIds);
        kafkaTemplate.send(TOPIC_ORDER_CANCELLED, orderTrackingNumber, event );
        log.info("[Kafka] Order cancelled event published for order: {}", orderTrackingNumber);
    }

    public void publishRefundOrder(String orderTrackingNumber, Long amount){
        OrderRefundEvent event = new OrderRefundEvent(orderTrackingNumber, amount);
        kafkaTemplate.send(TOPIC_REFUND_ORDER, orderTrackingNumber, event);
        log.info("[Kafka] Order refund event published for order: {}", orderTrackingNumber);
    }
}

