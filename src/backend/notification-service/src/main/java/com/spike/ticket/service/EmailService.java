package com.spike.ticket.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lớp này đại diện cho chi tiết của một vé trong email, bao gồm loại vé và mã QR đã được mã hóa Base64.
 */
record TicketEmailDetail(String categoryName, String base64QrCode) {}

@Service
@Slf4j
public class EmailService {

    @Value("${spring.sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${app.sendgrid.sender-email}")
    private String senderEmail;

    @Value("${app.sendgrid.template-id.ticket-qr}")
    private String orderSuccessTemplateId;

    @Value("${app.sendgrid.template-id.event-approved}")
    private String eventApprovedTemplateId;

    public void sendTicketEmail(String eventName, String toEmail, String customerName, String orderTrackingNumber, List<TicketEmailDetail> tickets) throws IOException {

        // 1. Thông tin người gửi và người nhận
        Email from = new Email(senderEmail, "Spike Ticket");
        Email to = new Email(toEmail);
        // 2. Khởi tạo Mail object
        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setTemplateId(orderSuccessTemplateId);

        // 3. Chuẩn bị dữ liệu động và các file đính kèm
        Personalization personalization = new Personalization();
        personalization.addTo(to);
        personalization.addDynamicTemplateData("customerName", customerName);
        personalization.addDynamicTemplateData("orderTrackingNumber", orderTrackingNumber);
        personalization.addDynamicTemplateData("eventName", eventName);

        List<Map<String, String>> ticketTemplateData = new ArrayList<>();

        for (int i = 0; i < tickets.size(); i++) {
            TicketEmailDetail ticket = tickets.get(i);
            String base64QrCode = ticket.base64QrCode();

            if (base64QrCode == null || base64QrCode.trim().isEmpty()) {
                continue; // Bỏ qua nếu không có mã QR
            }

            // Xử lý chuỗi base64
            String pureBase64 = base64QrCode.contains(",") ? base64QrCode.split(",")[1] : base64QrCode;

            // Tạo Content ID duy nhất cho mỗi mã QR
            String contentId = "qr_code_" + i;

            // Tạo attachment cho mã QR này
            Attachments attachments = new Attachments();
            attachments.setContent(pureBase64);
            attachments.setType("image/png");
            attachments.setFilename("ticket-qrcode-" + i + ".png");
            attachments.setDisposition("inline");
            attachments.setContentId(contentId);
            mail.addAttachments(attachments);

            // Tạo dữ liệu cho template Handlebars
            Map<String, String> data = new HashMap<>();
            data.put("ticket_type", ticket.categoryName());
            data.put("qr_url", "cid:" + contentId); // Tham chiếu đến attachment bằng Content ID
            ticketTemplateData.add(data);
        }

        // Thêm danh sách vé vào dữ liệu động của template
        personalization.addDynamicTemplateData("tickets", ticketTemplateData);
        mail.addPersonalization(personalization);


        // 4. Gọi API của SendGrid để gửi đi
        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("Gửi mail thành công cho đơn hàng {}. Trạng thái HTTP: {}", orderTrackingNumber, response.getStatusCode());
            } else {
                log.error("Gửi mail thất bại cho đơn hàng {}. Trạng thái HTTP: {}. Body: {}", orderTrackingNumber, response.getStatusCode(), response.getBody());
            }

        } catch (IOException ex) {
            System.err.println("Lỗi khi gọi SendGrid API cho đơn hàng " + orderTrackingNumber + ": " + ex.getMessage());
            throw ex;
        }
    }

    public void sendEmailEventApproved( String eventName, String toEmail, String username){
            Email from = new Email(senderEmail, "Spike Ticket");
            Email to = new Email(toEmail);

            Mail mail = new Mail();
            mail.setFrom(from);
            mail.setTemplateId(eventApprovedTemplateId);

            Personalization personalization = new Personalization();
            personalization.addTo(to);

            personalization.addDynamicTemplateData("eventName", eventName);
            personalization.addDynamicTemplateData("username", username);
            mail.addPersonalization(personalization);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            try {
                request.setMethod(Method.POST);
                request.setEndpoint("mail/send");
                request.setBody(mail.build());

                Response response = sg.api(request);

                if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                    log.info("Gửi mail thông báo sự kiện được duyệt thành công cho {}. Trạng thái HTTP: {}", toEmail, response.getStatusCode());
                } else {
                    log.error("Gửi mail thông báo sự kiện được duyệt thất bại cho {}. Trạng thái HTTP: {}. Body: {}", toEmail, response.getStatusCode(), response.getBody());
                }

            } catch (IOException ex) {
                log.error("Lỗi khi gọi SendGrid API để gửi thông báo sự kiện được duyệt cho {}: {}", toEmail, ex.getMessage());
            }
    }
}
