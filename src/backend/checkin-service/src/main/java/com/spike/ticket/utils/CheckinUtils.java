package com.spike.ticket.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spike.ticket.dto.CheckinResult;
import com.spike.ticket.dto.TicketMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;


@Component
@RequiredArgsConstructor
public class CheckinUtils
{

    private final StringRedisTemplate redisTemplate ;
    private final ObjectMapper objectMapper;
    
    public String signHmacSha256(String data, String secretKey) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            byte[] rawHmac = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Convert byte array sang chuỗi Hex
            StringBuilder hexString = new StringBuilder();
            for (byte b : rawHmac) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo chữ ký HMAC SHA256", e);
        }
    }

    public CheckinResult verifyQrData(String qrData, String secretKey) {
        if (qrData == null || !qrData.contains(".")) {
            return new CheckinResult(false,"", "Vé không hợp lệ");
        }

        // dạng chuỗi eventID|categoryId|ticketNumber.signature
        // 1. Tách chuỗi ra làm 2 phần

        String payload = qrData.split("\\.")[0]; // Phần dữ liệu gốc trước dấu chấm
        String providedSignature = qrData.split("\\.")[1]; // Phần chữ ký sau dấu chấm

        // 2. Tự tính toán lại chữ ký từ dữ liệu gốc và Secret Key nội bộ
        String expectedSignature = signHmacSha256(payload, secretKey);

        // 3. So sánh 2 chữ ký
        boolean success = MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                providedSignature.getBytes(StandardCharsets.UTF_8)
        );

        if (!success) {
            return new CheckinResult(false,"", "Vé không hợp lệ");
        }

        // Nếu chữ ký hợp lệ, tiếp tục lấy thông tin
        String categoryName = "";
        String categoryId = payload.split("\\|")[1];
        String ticketNumber = payload.split("\\|")[2];
        String redisKey = "ticket:category:" + categoryId + ":metadata";
        String jsonMetadata = redisTemplate.opsForValue().get(redisKey);

        if (jsonMetadata == null) {
            // null chứng tỏ trên redis đã xóa metadata do sự kiện kết thúc
            return new CheckinResult(false,ticketNumber, "Sự kiện đã kết thúc");
        }
        
        try {
            TicketMetadata metadata = objectMapper.readValue(jsonMetadata, TicketMetadata.class);
            categoryName = metadata.getCategoryName();
        } catch (JsonProcessingException e) {
            // Ghi log lỗi và trả về thông báo chung chung
            // log.error("Lỗi khi đọc metadata của vé: {}", categoryId, e);
            return new CheckinResult(false, ticketNumber, "Lỗi khi đọc thông tin vé");
        }
        
        return new CheckinResult(true,ticketNumber,
                String.format("Quét mã thành công, số vé : %s, loại vé: %s", ticketNumber, categoryName));
    }
}