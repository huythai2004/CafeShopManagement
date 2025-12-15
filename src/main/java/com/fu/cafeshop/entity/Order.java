package com.fu.cafeshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "table_id")
    private CafeTable cafeTable;

    @Column(name = "guest_name", length = 100)
    private String guestName;

    @Column(name = "guest_phone", length = 20)
    private String guestPhone;

    @Column(nullable = false, length = 20)
    private String status; // PENDING, APPROVED, COOKING, IN_PROGRESS, DONE, CANCELLED, REFUNDED

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "customer_notes", length = 1000)
    private String customerNotes;

    @ManyToOne
    @JoinColumn(name = "created_by_staff_id")
    private User createdByStaff;

    @ManyToOne
    @JoinColumn(name = "approved_by_staff_id")
    private User approvedByStaff;

    @Column(name = "estimated_completion_time")
    private LocalDateTime estimatedCompletionTime;

    @Column(name = "actual_completion_time")
    private LocalDateTime actualCompletionTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Payment payment;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (subtotal == null) subtotal = BigDecimal.ZERO;
        if (taxAmount == null) taxAmount = BigDecimal.ZERO;
        if (discountAmount == null) discountAmount = BigDecimal.ZERO;
        if (totalAmount == null) totalAmount = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getStatusDisplay() {
        return switch (status) {
            case "PENDING" -> "Chờ xác nhận";
            case "APPROVED" -> "Đã xác nhận";
            case "COOKING" -> "Đang pha chế";
            case "IN_PROGRESS" -> "Đang xử lý";
            case "DONE" -> "Hoàn thành";
            case "CANCELLED" -> "Đã hủy";
            case "REFUNDED" -> "Đã hoàn tiền";
            default -> status;
        };
    }

    public String getStatusBadgeClass() {
        return switch (status) {
            case "PENDING" -> "bg-yellow-100 text-yellow-800";
            case "APPROVED" -> "bg-blue-100 text-blue-800";
            case "COOKING" -> "bg-orange-100 text-orange-800";
            case "IN_PROGRESS" -> "bg-purple-100 text-purple-800";
            case "DONE" -> "bg-green-100 text-green-800";
            case "CANCELLED" -> "bg-red-100 text-red-800";
            case "REFUNDED" -> "bg-gray-100 text-gray-800";
            default -> "bg-gray-100 text-gray-800";
        };
    }

    public void calculateTotals() {
        this.subtotal = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalAmount = this.subtotal.add(this.taxAmount).subtract(this.discountAmount);
    }
}

