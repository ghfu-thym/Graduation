package com.spike.ticket.controller;

import com.spike.ticket.dto.SendTestEventApproved;
import com.spike.ticket.dto.TicketCreatedEvent;
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
    public ResponseEntity<?> sendTestEmailTicket(
            @RequestBody TicketCreatedEvent event
    ) {
        notificationService.sendTicketByOrder(event);
        return ResponseEntity.ok().build();
    }

    /** mẫu
     * {
     *   "orderTrackingNumber": "ORDER-12345-XYZ",
     *   "eventName": "Đại nhạc hội Mùa hè Sôi động 2026",
     *   "username": "Nguyễn Văn A",
     *   "email": "22028217@vnu.edu.vn",
     *   "tickets": [
     *     {
     *       "categoryId": 1,
     *       "categoryName": "Vé VIP",
     *       "ticketNumber": "TICKET-001",
     *       "qrCode": "https://www.youtube.com/watch?v=HwXR4Baqeak"
     *     },
     *     {
     *       "categoryId": 2,
     *       "categoryName": "Vé Thường",
     *       "ticketNumber": "TICKET-002",
     *       "qrCode": "https://www.youtube.com/watch?v=KOzFn7gTEjU"
     *     },
     *     {
     *       "categoryId": 2,
     *       "categoryName": "Vé Thường",
     *       "ticketNumber": "TICKET-003",
     *       "qrCode": "https://www.youtube.com/watch?v=SEJDQS_fK-I"
     *     }
     *   ]
     * }
     */

    @PostMapping("send-event-approved-email")
    public ResponseEntity<?> sendTestEmailEventApproved(
            @RequestBody SendTestEventApproved request){
        try {
            notificationService.sendEventApprovedNotification(
                    request.getEventName(),
                    request.getEmail(),
                    request.getUsername()
            );
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi khi gửi email thông báo sự kiện được duyệt: " + e.getMessage());
        }

    }
    /** template
     * {
     *   "eventName": "Sự kiện âm nhạc Mùa Hè 2026",
     *   "email": "your-email@example.com",
     *   "username": "Tên Người Dùng"
     * }
     */
}

