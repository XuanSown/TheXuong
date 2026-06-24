package com.example.thexuong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Voucher user đã sở hữu (sau khi redeem từ Vouchers catalog).
 * Mỗi row = 1 lần user đổi voucher, có mã DUY NHẤT để dùng khi checkout.
 *
 * Lifecycle:
 * - UNUSED: vừa đổi, chưa dùng, còn hạn
 * - USED: đã áp vào đơn hàng (set used_at + used_in_order_id)
 * - EXPIRED: quá 30 ngày kể từ issued_at, chưa dùng (cron expire)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "UserVouchers")
public class UserVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "voucher_id", nullable = false)
    private Long voucherId;

    /** Mã DUY NHẤT user nhận (UNIQUE). VD: TX-ABCDEF. Dùng khi checkout. */
    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "issued_at", updatable = false)
    private LocalDateTime issuedAt;

    /** = issuedAt + 30 ngày. */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "used_in_order_id")
    private Long usedInOrderId;

    public enum Status {
        UNUSED,
        USED,
        EXPIRED
    }
}
