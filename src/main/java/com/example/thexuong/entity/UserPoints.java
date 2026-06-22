package com.example.thexuong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Số dư điểm của user (1 user = 1 row).
 * Dùng {@code @Version} cho optimistic lock chống race condition khi 2 request cùng cộng/trừ điểm.
 *
 * Quy tắc:
 * - current_points có thể = 0 (sau spend/reverse hết), KHÔNG âm (service clamp về 0)
 * - total_earned cộng dồn, KHÔNG giảm khi spend/reverse (để track VIP tier)
 * - last_activity_at dùng cho logic expire 12 tháng (nếu user 12 tháng không có giao dịch → expire)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "UserPoints")
public class UserPoints {

    @Id
    @Column(name = "user_id")
    private Long userId;

    /** Số dư hiện tại. Service đảm bảo KHÔNG âm. */
    @Builder.Default
    @Column(name = "current_points", nullable = false)
    private Integer currentPoints = 0;

    /** Tổng điểm đã earn cộng dồn (chỉ tăng khi EARN, không giảm khi SPEND/REVERSE). */
    @Builder.Default
    @Column(name = "total_earned", nullable = false)
    private Long totalEarned = 0L;

    /** Tổng điểm đã tiêu (spend/reverse/expire) cộng dồn. */
    @Builder.Default
    @Column(name = "total_spent", nullable = false)
    private Long totalSpent = 0L;

    /** Thời điểm giao dịch gần nhất (EARN hoặc SPEND). Dùng cho logic expire 12 tháng. */
    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    /** Optimistic lock — Hibernate tự động tăng version khi update. */
    @Version
    @Builder.Default
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    /**
     * Quan hệ 1-1 với User (chỉ để dễ truy vấn, không bắt buộc cho business logic).
     * insertable=false, updatable=false vì user_id đã là PK + FK.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
}
