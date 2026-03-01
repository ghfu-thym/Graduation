package com.spike.ticket.dto;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString // Để log ra khi debug
public class WebhookPayload {
    private String orderTrackingNumber;
    private String providerTransactionId;

    private int responseCode;
    private String responseMessage;
    private BigDecimal amount;

    private String signature;

    private String rawData;
}
