package com.spike.ticket.dto;

import lombok.Data;

@Data
public class MomoPaymentResponse {
    private String partnerCode;
    private String orderId;
    private String requestId;
    private Long amount;
    private Long responseTime;
    private String message;
    private Integer resultCode;
    private String payUrl; // ĐÂY LÀ CÁI LINK ĐỂ GỬI CHO KHÁCH
    private String shortLink;
}
