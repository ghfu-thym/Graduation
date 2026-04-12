package com.spike.ticket.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class EmailService {

    @Value("${spring.sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${app.sendgrid.sender-email}")
    private String senderEmail;

    @Value("${app.sendgrid.template-id.ticket-qr}")
    private String orderSuccessTemplateId;

    public void sendTicketEmail(String toEmail, String customerName, String orderTrackingNumber, String base64QrCode) throws IOException {

        // 1. Thông tin người gửi và người nhận
        Email from = new Email(senderEmail, "Spike Ticket");
        Email to = new Email(toEmail);

        // 2. Khởi tạo Mail object
        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setTemplateId(orderSuccessTemplateId);

        // 3. Truyền dữ liệu động (Dynamic Data) vào Template
        Personalization personalization = new Personalization();
        personalization.addTo(to);
        personalization.addDynamicTemplateData("customerName", customerName);
        personalization.addDynamicTemplateData("orderTrackingNumber", orderTrackingNumber);
        mail.addPersonalization(personalization);

        // 4. Đính kèm mã QR (Đã nhận trực tiếp chuỗi String)
        if (base64QrCode != null && !base64QrCode.trim().isEmpty()) {

            // XỬ LÝ QUAN TRỌNG: Loại bỏ tiền tố "data:image/png;base64," nếu có
            String pureBase64 = base64QrCode;
            if (base64QrCode.contains(",")) {
                pureBase64 = base64QrCode.split(",")[1];
            }

            Attachments attachments = new Attachments();
            attachments.setContent(pureBase64); // Truyền trực tiếp chuỗi Base64 sạch
            attachments.setType("image/png");
            attachments.setFilename("ticket-qrcode-" + orderTrackingNumber + ".png");
            attachments.setDisposition("inline");
            attachments.setContentId("QR_CODE_ID");

            mail.addAttachments(attachments);
        }

        // 5. Gọi API của SendGrid để gửi đi
        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            System.out.println("Gửi mail thành công. Trạng thái HTTP: " + response.getStatusCode());

        } catch (IOException ex) {
            System.err.println("Lỗi khi gọi SendGrid API: " + ex.getMessage());
            throw ex;
        }
    }
}
