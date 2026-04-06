package com.spike.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CheckinDto {

    /**
     * DTO Hứng dữ liệu từ Mobile App gửi lên
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScanRequest {

        @NotBlank(message = "Dữ liệu mã QR không được để trống")
        private String qrData;


        @NotNull(message = "ID sự kiện không được để trống")
        private Long eventId;
    }

    /**
     * DTO Trả kết quả về cho Mobile App hiển thị
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScanResponse {

        private boolean success;
        private String message;  // Nội dung thông báo chi tiết

    }
}