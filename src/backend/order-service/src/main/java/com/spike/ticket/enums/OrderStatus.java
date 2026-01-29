package com.spike.ticket.enums;

public enum OrderStatus {
    PENDING, // Đang chờ thanh toán
    PAID, // Đã thanh toán
    RESERVED, // Đã giữ chỗ
    CANCELLED, // Hủy
    TIMEOUT, // Hết thời gian giữ chỗ
}
