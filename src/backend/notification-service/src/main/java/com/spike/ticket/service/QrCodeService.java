package com.spike.ticket.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
public class QrCodeService {

    /**
     * Tạo mã QR từ một chuỗi văn bản và trả về định dạng Base64
     *
     * @param text   Nội dung mã QR (Ví dụ: Link kiểm tra vé)
     * @param width  Chiều rộng ảnh (pixel)
     * @param height Chiều cao ảnh (pixel)
     * @return Chuỗi ảnh đã mã hóa Base64
     */
    public String generateQrCodeBase64(String text, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            // Tạo ma trận điểm ảnh (BitMatrix) cho QR Code
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

            // Viết ma trận này vào một luồng bộ nhớ (ByteArray) dưới định dạng PNG
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

            // Chuyển mảng byte thành chuỗi Base64
            byte[] pngData = pngOutputStream.toByteArray();
            return Base64.getEncoder().encodeToString(pngData);

        } catch (Exception e) {
            System.err.println("Lỗi khi tạo mã QR: " + e.getMessage());
            return null;
        }
    }
}
