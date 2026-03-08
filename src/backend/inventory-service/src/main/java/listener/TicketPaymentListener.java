package listener;

import com.spike.ticket.dto.ConfirmTicketRequest;
import com.spike.ticket.dto.OrderConfirmedEvent;
import com.spike.ticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TicketPaymentListener {

    private final TicketService ticketService;

    @KafkaListener(topics = "order-confirmed-events", groupId = "ticket-group")
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        log.info("[Kafka listener] Received order confirmed event for order: {}, ticketIds: {}",
                event.orderTrackingNumber(), event.ticketIds());

        try {
            // tận dụng lại DTO từ lúc dùng feign để
            ConfirmTicketRequest request = new ConfirmTicketRequest();
            request.setTicketIds(event.ticketIds());
            request.setOrderTrackingNumber(event.orderTrackingNumber());
            ticketService.confirmTickets(request);
            log.info("[Kafka listener] Tickets confirmed for order: {}", event.orderTrackingNumber());
        } catch (Exception e) {
            log.error("[Kafka listener] Error confirming tickets for order {}: {}",
                    event.orderTrackingNumber(), e.getMessage());
            //TODO: dead letter queue
        }
    }
}
