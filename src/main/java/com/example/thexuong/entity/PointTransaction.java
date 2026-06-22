package com.example.thexuong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Lịch sử giao dịch điểm. Mỗi lần earn/spend/reverse/expire đều tạo 1 row.
 * FIFO expire theo created_at (chỉ áp dụng cho EARN, vì chỉ EARN có expires_at).
 *
 * Type:
 * - EARN: cộng điểm (dương). Set expires_at = created_at + 12 tháng.
 * - SPEND: tiêu điểm đổi voucher (âm). KHÔNG có expires_at.
 * - REVERSE: hoàn điểm khi refund (âm). KHÔNG có expires_at.
 * - EXPIRE: điểm hết hạn (âm, do cron tạo). KHÔNG có expires_at.
 * - ADJUST: admin chỉnh sửa thủ công (có admin_id + note bắt buộc).
 */
@Data
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

    /** FK tới UserVouchers (Batch 2 sẽ tạo bảng). Hiện tại để nullable, không có FK constraint. */
    @Column(name = "user_voucher_id")
    private Long userVoucherId;

    /** EARN / SPEND / REVERSE / EXPIRE / ADJUST */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Type type;

    /** Dương cho EARN, âm cho SPEND/REVERSE/EXPIRE. */
    @Column(nullable = false)
    private Integer points;

    /** Chỉ set cho EARN (EARN + 12 tháng). Null với các type khác. */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /** Admin thực hiện ADJUST (audit). Null với EARN/SPEND/REVERSE/EXPIRE. */
    @Column(name = "admin_id")
    private Long adminId;

    @Column(length = 500)
    private String note;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum Type {
        EARN,    // cộng điểm từ đơn COMPLETED
        SPEND,   // tiêu điểm đổi voucher
        REVERSE, // hoàn điểm khi refund
        EXPIRE,  // điểm hết hạn (cron)
        ADJUST   // admin chỉnh sửa thủ công
    }
}
