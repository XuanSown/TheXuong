package com.example.thexuong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Phân hạng thành viên: THUONG (Khách hàng thường) / VIP (Khách hàng VIP).
 * Áp dụng Phương án C: lên hạng khi đạt min_total_spent HOẶC min_total_points.
 * Áp dụng Phương án Y: hạ hạng theo cron re-evaluate 365 ngày.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "PointTiers")
public class PointTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mã hạng: THUONG / VIP (UNIQUE) */
    @Column(nullable = false, unique = true, length = 20)
    private String code;

    /** Tên hiển thị: "Khách hàng thường" / "Khách hàng VIP" */
    @Column(nullable = false, length = 50)
    private String name;

    /** Ngưỡng tổng chi tiêu (VND) để lên hạng — Phương án C chi tiêu */
    @Builder.Default
    @Column(name = "min_total_spent", nullable = false)
    private java.math.BigDecimal minTotalSpent = java.math.BigDecimal.ZERO;

    /** Ngưỡng tổng điểm earn để lên hạng — Phương án C điểm */
    @Builder.Default
    @Column(name = "min_total_points", nullable = false)
    private Integer minTotalPoints = 0;

    /**
     * JSON benefits: {vipBonus: bool, freeShipping: bool, voucherOnly: bool, prioritySupport: bool}.
     * Lưu NVARCHAR(MAX) dạng JSON string — tự parse ở service nếu cần.
     */
    @Column(name = "benefits", columnDefinition = "NVARCHAR(MAX)")
    private String benefits;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
