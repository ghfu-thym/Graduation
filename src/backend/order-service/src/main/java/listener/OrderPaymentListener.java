package listener;

import com.spike.ticket.dto.event.PaymentSuccessEvent;
import com.spike.ticket.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderPaymentListener {

    private final OrderService orderService;

    @KafkaListener(topics = "payment-success-events", groupId = "order-group")
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        String orderTrackingNumber = event.orderTrackingNumber();
        String transactionId = event.transactionId();
        log.info("[Kafka listener] Received payment success event for order: {}", event.orderTrackingNumber());

        try {
            orderService.completePayment(orderTrackingNumber, transactionId);
            log.info("[Kafka listener] Payment completed for order: {}", orderTrackingNumber);

        } catch (Exception e) {
            log.error("[Kafka listener] Error processing payment success event: {}", e.getMessage());

            //TODO: dead letter queue
        }
    }
}
