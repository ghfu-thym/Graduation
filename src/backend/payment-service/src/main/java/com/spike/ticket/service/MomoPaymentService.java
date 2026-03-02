package com.spike.ticket.service;

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
    @Value("${momo.endpoint}")
    private String endpoint;

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

        System.out.println("=== CHUỖI CỦA MOMO ===");
        System.out.println("accessKey=klm05TvNCzjfasWj&amount=50000&extraData=&ipnUrl=" + ipnUrl + "&orderId=" + orderTrackingNumber + "&orderInfo=" + orderInfo + "&partnerCode=MOMOBKUN20180529&redirectUrl=" + redirectUrl + "&requestId=" + requestId + "&requestType=captureWallet");
        System.out.println("=== CHUỖI CỦA BẠN ===");
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
                    .uri(endpoint)
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
}
