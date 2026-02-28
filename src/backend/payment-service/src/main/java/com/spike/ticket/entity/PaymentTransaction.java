package com.spike.ticket.entity;

import com.spike.ticket.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(name = "order_tracking_number", nullable = false)
    private String orderTrackingNumber;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", length = 10)
    @Builder.Default
    private String currency = "VND";

    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;

    // Mã giao dịch từ VNPAY/Momo trả về
    @Column(name = "provider_transaction_id", length = 100)
    private String providerTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    // Khóa duy nhất để chặn Cổng thanh toán gọi Webhook nhiều lần cho 1 trạng thái
    //Khi Webhook bắn về, bạn tạo key này theo format: Webhook_{OrderId}_{ProviderTransactionId}_{Trạng thái}.
    // Nếu cổng thanh toán spam Webhook lần thứ 2, MySQL sẽ ném lỗi DataIntegrityViolationException ngay lập tức,
    // bảo vệ hệ thống không bị cộng tiền hay chốt ticket 2 lần
    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

