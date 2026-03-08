package com.spike.ticket.dto;

public record MoMoRefundRequest(
        String partnerCode,
        String orderId,      // Bắt buộc là mã đơn hoàn tiền MỚI
        String requestId,    // Định danh duy nhất cho request này
        Long amount,         // Số tiền cần hoàn
        Long transId,        // Mã giao dịch MoMo gốc (lấy từ event thanh toán)
        String lang,         // "vi" hoặc "en"
        String description,  // Mô tả (không được null, có thể rỗng)
        String signature     // Chữ ký băm HMAC-SHA256
) {}