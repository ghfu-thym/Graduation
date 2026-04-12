package com.spike.ticket.controller;

import com.spike.ticket.dto.SendTestEmailRequest;
import com.spike.ticket.service.EmailService;
import com.spike.ticket.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping("/send-test-email")
    public ResponseEntity<?> sendTestEmail(
            @RequestBody SendTestEmailRequest request
    ) {
        notificationService.sendTicket(request);
        return ResponseEntity.ok().build();
    }
}
