package com.spike.ticket.dto;

import lombok.*;

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
    private Long amount;

    private String signature;

    private String rawData;
}
