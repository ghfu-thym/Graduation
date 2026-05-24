package com.spike.ticket.service;


import com.spike.ticket.dto.EventApprovedInventoryMessage;
import com.spike.ticket.dto.TicketCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailService emailService;
    private final QrCodeService qrCodeService;

    public void sendTicketByOrder(TicketCreatedEvent event) {
        try {
            // 1. Tạo danh sách chi tiết vé cho email từ event
            List<TicketEmailDetail> ticketDetailsForEmail = event.tickets().stream()
                    .map(ticketDetail -> {
                        try {
                            // Tạo mã QR cho từng vé
                            String qrCodeBase64 = qrCodeService.generateQrCodeBase64(ticketDetail.qrCode(), 250, 250);
                            // Trả về đối tượng TicketEmailDetail chứa thông tin cần thiết
                            return new TicketEmailDetail(ticketDetail.categoryName(), qrCodeBase64);
                        } catch (Exception e) {
                            log.error("Lỗi khi tạo mã QR cho vé {}: {}", ticketDetail.ticketNumber(), e.getMessage());
                            return null; // Trả về null nếu có lỗi
                        }
                    })
                    .filter(detail -> detail != null && detail.base64QrCode() != null && !detail.base64QrCode().isEmpty())
                    .collect(Collectors.toList());

            // 2. Kiểm tra xem có vé nào được xử lý thành công không
            if (ticketDetailsForEmail.isEmpty()) {
                log.error("Không thể tạo bất kỳ mã QR nào cho đơn hàng: {}", event.orderTrackingNumber());
                return; // Không gửi email nếu không có QR code
            }

            // 3. Gọi EmailService để gửi email với danh sách chi tiết vé
            emailService.sendTicketEmail(
                    event.eventName(),
                    event.email(),
                    event.username(),
                    event.orderTrackingNumber(),
                    ticketDetailsForEmail
            );
        } catch (Exception e) {
            log.error("Lỗi khi gửi email vé cho đơn hàng {}: {}", event.orderTrackingNumber(), e.getMessage(), e);
        }
    }

    public void sendEventApprovedNotification(String eventName, String organizerEmail, String organizationName) {
        emailService.sendEmailEventApproved(
                eventName,
                organizerEmail,
                organizationName
        );
    }
}
