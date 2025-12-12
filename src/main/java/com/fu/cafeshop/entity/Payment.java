package com.fu.cafeshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(name = "payment_method", nullable = false, length = 20)
    private String paymentMethod = "CASH"; // CASH, CARD, QR_CODE, WALLET

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 20)
    private String status; // PENDING, COMPLETED, FAILED, CANCELLED, REFUNDED

    @Column(name = "transaction_id", length = 200)
    private String transactionId;

    @Column(name = "gateway_response", length = 2000)
    private String gatewayResponse;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (paymentMethod == null) paymentMethod = "CASH";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getStatusDisplay() {
        return switch (status) {
            case "PENDING" -> "Chờ thanh toán";
            case "COMPLETED" -> "Đã thanh toán";
            case "FAILED" -> "Thất bại";
            case "CANCELLED" -> "Đã hủy";
            case "REFUNDED" -> "Đã hoàn tiền";
            default -> status;
        };
    }

    public String getPaymentMethodDisplay() {
        return switch (paymentMethod) {
            case "CASH" -> "Tiền mặt";
            case "CARD" -> "Thẻ";
            case "QR_CODE" -> "QR Code";
            case "WALLET" -> "Ví điện tử";
            default -> paymentMethod;
        };
    }
}

