package com.spike.ticket.dto;

import lombok.Data;

@Data
public class SendTestEmailRequest {
    private String toEmail;
    private String customerName;
    private String orderTrackingNumber;
    private String qrCodeBase64;
}
