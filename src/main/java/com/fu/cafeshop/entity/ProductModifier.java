package com.fu.cafeshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_modifiers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductModifier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 100)
    private String name; // e.g., "Extra shot", "Syrup", "Size L"

    @Column(name = "price_delta", precision = 12, scale = 2)
    private BigDecimal priceDelta = BigDecimal.ZERO;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (priceDelta == null) priceDelta = BigDecimal.ZERO;
        if (isDefault == null) isDefault = false;
    }
}

