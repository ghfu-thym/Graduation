package com.spike.ticket.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MomoPaymentRequest {
    private String partnerCode;
    private String requestId;
    private Long amount;
    private String orderId; // orderTrackingNumber
    private String orderInfo;
    private String redirectUrl; // Trả về Frontend
    private String ipnUrl;      // Webhook (Ngrok)
    private String requestType; // Mặc định là "captureWallet" (Thanh toán qua ví)
    private String extraData;
    private String lang;
    private String signature;
}
