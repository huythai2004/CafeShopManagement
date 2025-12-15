package com.fu.cafeshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cafe_tables")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CafeTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_number", nullable = false, unique = true)
    private Integer tableNumber;

    @Column(length = 50)
    private String name;

    @Column
    @Builder.Default
    private Integer capacity = 4;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (name == null || name.isBlank()) {
            name = "Bàn " + tableNumber;
        }
    }

    public String getDisplayName() {
        return name != null ? name : "Bàn " + tableNumber;
    }
}


