package com.spike.ticket.service;

import com.spike.ticket.dto.MoMoRefundRequest;
import com.spike.ticket.dto.MoMoRefundResponse;
import com.spike.ticket.dto.MomoPaymentRequest;
import com.spike.ticket.dto.MomoPaymentResponse;
import com.spike.ticket.utils.HmacUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class MomoPaymentService {
    @Value("${momo.payment-endpoint}")
    private String paymentEndpoint;

    @Value("${momo.refund-endpoint}")
    private String refundEndpoint;

    @Value("${momo.partner-code}")
    private String partnerCode;

    @Value("${momo.access-key}")
    private String accessKey;

    @Value("${momo.secret-key}")
    private String secretKey;

    @Value("${momo.redirect-url}")
    private String redirectUrl;

    @Value("${momo.ipn-url}")
    private String ipnUrl;

    private final RestClient momoRestClient;

    public String createPaymentLink(String orderTrackingNumber, Long amount) {
        String requestId = UUID.randomUUID().toString();
        String orderInfo = "Thanh toan don hang: " + orderTrackingNumber;
        String requestType = "payWithMethod";
        String extraData = "";
        String lang = "vi";

        String rawData = "accessKey="+accessKey+
                "&amount="+amount+
                "&extraData="+extraData+
                "&ipnUrl="+ipnUrl+
                "&orderId="+orderTrackingNumber+
                "&orderInfo="+orderInfo+
                "&partnerCode="+partnerCode+
                "&redirectUrl="+redirectUrl+
                "&requestId="+requestId+
                "&requestType="+requestType;

        System.out.println(rawData);
        String signature = HmacUtils.signHmacSha256(rawData, secretKey);

        MomoPaymentRequest  request = MomoPaymentRequest.builder()
                .partnerCode(partnerCode)
                .requestId(requestId)
                .requestType(requestType)
                .ipnUrl(ipnUrl)
                .orderId(orderTrackingNumber)
                .orderInfo(orderInfo)
                .redirectUrl(redirectUrl)
                .extraData(extraData)
                .lang(lang)
                .amount(amount)
                .signature(signature)
                .build();

        // bắn sang server momo
        try {
            log.info("Sending payment request to Momo: {}", request);
            MomoPaymentResponse response = momoRestClient.post()
                    .uri(paymentEndpoint)
                    .body(request)
                    .retrieve()
                    .body(MomoPaymentResponse.class);

            if (response != null && response.getResultCode() == 0){
                return response.getPayUrl(); // link thanh toan thanh cong
            } else {
                log.error("Error from Momo: {}", response != null ? response.getMessage() : "Unknown");
                throw new RuntimeException("Cannot create pay url: " + (response != null ? response.getMessage() : "Unknown"));
            }
        } catch (Exception e) {
            log.error("Error to connect to momo api {}", e.getMessage());
            throw new RuntimeException("Error payment gateway: " + e.getMessage());
        }
    }

    public MoMoRefundResponse refundPayment(String originalOrderId,String transactionId, Long amount){
        Long transId = Long.parseLong(transactionId);
        String requestId = UUID.randomUUID().toString();

        String refundOrderId = "RF_" + originalOrderId + "_" + System.currentTimeMillis();
        String description = "Refund for order: " + originalOrderId;
        String rawSignature = "accessKey=" + accessKey +
                "&amount=" + amount +
                "&description=" + description +
                "&orderId=" + refundOrderId +
                "&partnerCode=" + partnerCode +
                "&requestId=" + requestId +
                "&transId=" + transId;
        String signature = HmacUtils.signHmacSha256(rawSignature, secretKey);

        MoMoRefundRequest request = new MoMoRefundRequest(
                partnerCode,
                refundOrderId,
                requestId,
                amount,
                transId,
                "vi",
                description,
                signature
        );

        MoMoRefundResponse response = momoRestClient.post()
                .uri(refundEndpoint)
                .body(request)
                .retrieve()
                .body(MoMoRefundResponse.class);

        return response;
    }
}
