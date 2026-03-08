package com.spike.ticket.kafka.listener;

import com.spike.ticket.dto.OrderRefundEvent;
import com.spike.ticket.dto.MoMoRefundResponse;
import com.spike.ticket.entity.PaymentTransaction;
import com.spike.ticket.enums.PaymentStatus;
import com.spike.ticket.repository.PaymentTransactionRepository;
import com.spike.ticket.service.MomoPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentEventListener {

    private final MomoPaymentService momoPaymentService;
    private final PaymentTransactionRepository paymentRepo;

    @KafkaListener(topics = "refund-order-events", groupId = "payment-group")
    public void handleOrderRefund(OrderRefundEvent event) {
        log.info("[Kafka] Received refund event for order: {}, amount: {}",
                event.orderTrackingNumber(), event.amount());

        try {
            // Tìm transaction trong DB để lấy providerTransactionId (transId của MoMo)
            PaymentTransaction transaction = paymentRepo.findByOrderTrackingNumber(event.orderTrackingNumber())
                    .orElseThrow(() -> new RuntimeException(
                            "Payment transaction not found for order: " + event.orderTrackingNumber()));

            if (transaction.getStatus() == PaymentStatus.REFUNDED) {
                log.info("[Kafka] Order {} already refunded, skipping.", event.orderTrackingNumber());
                return;
            }

            if (transaction.getProviderTransactionId() == null) {
                log.error("[Kafka] No provider transaction ID for order {}, cannot refund.",
                        event.orderTrackingNumber());
                return;
            }

            // Gọi MoMo refund API
            MoMoRefundResponse response = momoPaymentService.refundPayment(
                    event.orderTrackingNumber(),
                    transaction.getProviderTransactionId(),
                    event.amount()
            );

            // Cập nhật trạng thái trong DB
            if (response != null && response.resultCode() == 0) {
                transaction.setStatus(PaymentStatus.REFUNDED);
                paymentRepo.save(transaction);
                log.info("[Kafka] Refund successful for order: {}", event.orderTrackingNumber());
            } else {
                log.error("[Kafka] Refund failed for order: {}, resultCode: {}",
                        event.orderTrackingNumber(),
                        response != null ? response.resultCode() : "null response");
            }

        } catch (Exception e) {
            log.error("[Kafka] Error processing refund for order {}: {}",
                    event.orderTrackingNumber(), e.getMessage());
            //TODO: dead letter queue hoặc retry
        }
    }
}
