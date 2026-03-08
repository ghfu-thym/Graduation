package com.spike.ticket.kafka.publisher;

import com.spike.ticket.dto.PaymentSuccessEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    public static final String TOPIC_NAME = "payment-success-events";

    public PaymentEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(String orderTrackingNumber, String transactionId, Long amount) {
        PaymentSuccessEvent event = new PaymentSuccessEvent(orderTrackingNumber, transactionId, "SUCCESS", amount);
        kafkaTemplate.send(TOPIC_NAME, orderTrackingNumber, event);
        log.info("Payment success event published: {}", event);
    }
}
