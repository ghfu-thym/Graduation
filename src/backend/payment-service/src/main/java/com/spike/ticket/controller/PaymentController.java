package com.spike.ticket.controller;

import com.spike.ticket.dto.MomoIpnRequest;
import com.spike.ticket.dto.PaymentRequest;
import com.spike.ticket.dto.WebhookPayload;
import com.spike.ticket.service.MomoPaymentService;
import com.spike.ticket.service.WebhookService;
import com.spike.ticket.utils.HmacUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    private final WebhookService webhookService;
    private final MomoPaymentService momoPaymentService;

    @Value("${momo.secrete-key}")
    private String secreteKey;

    @Value("${momo.access-key}")
    private String accessKey;

    @PostMapping("/create-url")
    public ResponseEntity<String> createPaymentUrl(@RequestBody PaymentRequest request) {
        log.info("Request to create payment link for order {}", request.orderTrackingNumber());

        String PaymentUrl = momoPaymentService.createPaymentLink(request.orderTrackingNumber(), request.amount());

        return ResponseEntity.ok(PaymentUrl);
    }

    @PostMapping("/momo-ipn")
    public ResponseEntity<Void> handleMomoIPN(@RequestBody MomoIpnRequest request) {
        log.info("Received Momo IPN request for order id: {}", request.getOrderId());

        // kiem tra signature
        if (!verifySignature(request)){
            log.error("Invalid signature for order id: {}", request.getOrderId());
            return ResponseEntity.badRequest().build();
        }

        // map dto
        WebhookPayload payload = WebhookPayload.builder()
                .orderTrackingNumber(request.getOrderId())
                .providerTransactionId(String.valueOf(request.getTransId()))
                .responseCode(request.getResultCode())
                .responseMessage(request.getMessage())
                .amount(BigDecimal.valueOf(request.getAmount()))
                .signature(request.getSignature())
                .build();

        //xu ly payload
        webhookService.processWebhook(payload);

        // momo yeu cau tra ve HTTP 204 (No Content) khi da nhan duoc ipn
        return ResponseEntity.noContent().build();

    }

    @GetMapping("/momo-return")
    public ResponseEntity<String> handleMomoReturn(@RequestParam("orderId") String orderId) {
        log.info("Received Momo return request for order id: {}", orderId);
        return ResponseEntity.ok("Thanh toán thành công cho đơn " + orderId);
    }


    private boolean verifySignature(MomoIpnRequest request) {
        String rawData = "accessKey=" + accessKey +
                "&amount=" + request.getAmount() +
                "&extraData=" + request.getExtraData() +
                "&message=" + request.getMessage() +
                "&orderId=" + request.getOrderId() +
                "&orderInfo=" + request.getOrderInfo() +
                "&orderType=" + request.getOrderType() +
                "&partnerCode=" + request.getPartnerCode() +
                "&payType=" + request.getPayType() +
                "&requestId=" + request.getRequestId() +
                "&responseTime=" + request.getResponseTime() +
                "&resultCode=" + request.getResultCode() +
                "&transId=" + request.getTransId();

        String generatedSignature = HmacUtils.signHmacSha256(rawData, secreteKey);
        return generatedSignature.equals(request.getSignature());
    }
}
