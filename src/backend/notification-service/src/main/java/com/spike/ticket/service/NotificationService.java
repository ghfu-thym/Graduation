package com.spike.ticket.service;

import com.spike.ticket.dto.SendTestEmailRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailService emailService;

    private final QrCodeService qrCodeService;

    public void sendTicket (SendTestEmailRequest request) {
        try {
            String qrCodeBase64 = qrCodeService.generateQrCodeBase64(request.getQrCodeBase64(), 500, 500);
            emailService.sendTicketEmail(request.getToEmail(), request.getCustomerName(), request.getOrderTrackingNumber(), qrCodeBase64);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
