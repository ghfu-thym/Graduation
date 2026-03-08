package com.spike.ticket.service;

import com.spike.ticket.client.OrderClient;
import com.spike.ticket.dto.WebhookPayload;
import com.spike.ticket.entity.PaymentTransaction;
import com.spike.ticket.enums.PaymentStatus;
import com.spike.ticket.kafka.publisher.PaymentEventPublisher;
import com.spike.ticket.repository.PaymentTransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebhookService {
    private final PaymentTransactionRepository paymentRepo;
    private final StringRedisTemplate redisTemplate;
    private final OrderClient orderClient; // Feign client gọi sang order-service
    private final PaymentEventPublisher paymentEventPublisher;

    @Transactional
    public void processWebhook(WebhookPayload payload) {
        String orderTrackingNumber = payload.getOrderTrackingNumber();


        //Tạo khóa Redis (Distributed Lock) để chống race condition
        String redisKey = "order_timeout:"+orderTrackingNumber;
        // setIfAbsent tương đương lệnh SETNX trong Redis. Lock tồn tại trong 10 giây.
        Boolean isLocked = redisTemplate.opsForValue().setIfAbsent(redisKey, "LOCKED", Duration.ofSeconds(10));

        if (Boolean.FALSE.equals(isLocked)) {
            log.info("Order {} is already being processed. Skipping webhook.", orderTrackingNumber);
            return;
        }

        try {
            //query db kiêm tra idempotency
            PaymentTransaction transaction = paymentRepo.findByOrderTrackingNumber(orderTrackingNumber)
                    .orElseThrow(() -> new RuntimeException("Payment transaction not found for order ID: " + orderTrackingNumber));
            if (transaction.getStatus() == PaymentStatus.SUCCESS){
                log.info("Order {} is already paid. Skipping webhook.", orderTrackingNumber);
                return;
            }

            //TODO: xem response code của momo,vnpay là gì
            // response code = 0 la thanh cong
            if (payload.getResponseCode()==0){
                transaction.setStatus(PaymentStatus.SUCCESS);
                transaction.setProviderTransactionId(payload.getProviderTransactionId());
                paymentRepo.save(transaction);

                log.info("Order {} is paid successfully. Call to order-service", orderTrackingNumber);

                //orderClient.confirmOrderPaid(orderTrackingNumber);
                paymentEventPublisher.publish(orderTrackingNumber, payload.getProviderTransactionId(), payload.getAmount().longValue());
            } else {
                //khi thanh toán thất bại
                transaction.setStatus(PaymentStatus.FAILED);
                transaction.setErrorMessage(payload.getResponseMessage());
                paymentRepo.save(transaction);
            }
        } catch (Exception e) {
            log.error("Error processing webhook for order ID: {}, message: {}", orderTrackingNumber, e.getMessage());
        } finally {
            // nhả khóa
            redisTemplate.delete(redisKey);
        }


    }

}
