package com.spike.ticket.controller;

import com.spike.ticket.dto.WebhookPayload;
import com.spike.ticket.service.WebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class WebhookController {
    private final WebhookService webhookService;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody WebhookPayload payload) {
        webhookService.processWebhook(payload);
        return ResponseEntity.ok("SUCCESS");
    }
}
