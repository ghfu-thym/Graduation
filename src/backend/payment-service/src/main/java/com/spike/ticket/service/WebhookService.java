package com.spike.ticket.service;

import com.spike.ticket.dto.WebhookPayload;
import com.spike.ticket.entity.PaymentTransaction;
import com.spike.ticket.enums.PaymentStatus;
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
    // TODO: thêm feign client để gọi sang order-service
    // private final OrderClient orderClient; // Feign client gọi sang order-service

    @Transactional
    public void processWebhook(WebhookPayload payload) {
        String orderTrackingNumber = payload.getOrderTrackingNumber();

        // Kiểm tra chữ ký
        if (!isValidSignature(payload)) {
            log.error("Invalid signature for order tracking number: {}", orderTrackingNumber);
            throw new IllegalArgumentException("Invalid signature");

        }

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
            // giả sử response code "00" là thanh toán thành công
            if ("00".equals(payload.getResponseCode())){
                transaction.setStatus(PaymentStatus.SUCCESS);
                transaction.setProviderTransactionId(payload.getProviderTransactionId());
                paymentRepo.save(transaction);

                log.info("Order {} is paid successfully. Call to order-service", orderTrackingNumber);

                //TODO: call order service để chốt ticket
                // orderClient.confirmOrderPaid(orderTrackingNumber);
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

    private boolean isValidSignature(WebhookPayload payload){
        //test
       return true;
    }
}
