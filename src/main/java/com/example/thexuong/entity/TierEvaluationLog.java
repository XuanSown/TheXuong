package com.example.thexuong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Audit log cho mỗi lần cron re-evaluate tier (Phương án Y).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tier_evaluation_log")
public class TierEvaluationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "evaluated_at", updatable = false)
    private LocalDateTime evaluatedAt;

    @Column(name = "window_start", nullable = false)
    private LocalDateTime windowStart;

    @Column(name = "window_end", nullable = false)
    private LocalDateTime windowEnd;

    @Column(name = "total_spent")
    private java.math.BigDecimal totalSpent;

    @Column(name = "total_points_earned")
    private Integer totalPointsEarned;

    @Column(name = "old_tier_code", length = 20)
    private String oldTierCode;

    @Column(name = "new_tier_code", nullable = false, length = 20)
    private String newTierCode;

    @Column(length = 500)
    private String reason;
}
