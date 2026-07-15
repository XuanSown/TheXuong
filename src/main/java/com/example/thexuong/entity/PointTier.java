package com.example.thexuong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Phân hạng thành viên: THUONG (Khách hàng thường) / VIP (Khách hàng VIP).
 * Phương án C: lên hạng khi đạt min_total_spent HOẶC min_total_points.
 * Phương án Y: hạ hạng theo cron re-evaluate 365 ngày.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "PointTiers")
public class PointTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    @Builder.Default
    @Column(name = "min_total_spent", nullable = false)
    private BigDecimal minTotalSpent = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "min_total_points", nullable = false)
    private Integer minTotalPoints = 0;

    @Column(name = "benefits", columnDefinition = "NVARCHAR(MAX)")
    private String benefits;

    @Column(name = "bonus_percentage")
    private Integer bonusPercentage;

    @Column(name = "auto_discount_percent")
    private BigDecimal autoDiscountPercent;

    @Column(name = "reward_voucher_id")
    private Long rewardVoucherId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
