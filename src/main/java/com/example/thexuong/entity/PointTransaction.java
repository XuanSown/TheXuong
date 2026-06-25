package com.example.thexuong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Lịch sử giao dịch điểm. FIFO expire theo created_at (chỉ áp dụng cho EARN).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "PointTransactions")
public class PointTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "user_voucher_id")
    private Long userVoucherId;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(nullable = false)
    private Integer points;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "admin_id")
    private Long adminId;

    @Column(length = 500)
    private String note;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum Type {
        EARN, SPEND, REVERSE, EXPIRE, ADJUST
    }
}
