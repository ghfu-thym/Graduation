package com.spike.ticket.dto;

import lombok.Data;

@Data
public class MomoIpnRequest {
    private String partnerCode;
    private String orderId;       // Tương ứng với orderTrackingNumber của bạn
    private String requestId;
    private Long amount;
    private String orderInfo;
    private String orderType;
    private Long transId;         // Mã giao dịch do MoMo cấp (dùng để đối soát)
    private Integer resultCode;   // 0 nghĩa là thanh toán thành công
    private String message;
    private String payType;
    private Long responseTime;
    private String extraData;
    private String signature;
}
