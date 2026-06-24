package com.example.thexuong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Audit log cho mọi thay đổi Voucher catalog (CREATE / UPDATE / DELETE / LOCK / UNLOCK).
 *
 * Schema theo ADMIN_VOUCHER_REQUIREMENTS.md → VoucherAuditLog Table.
 * - old_values / new_values: JSON snapshot (NVARCHAR(MAX)) — lưu trước/sau khi thay đổi.
 * - changed_fields: JSON array tên field đã đổi (vd: ["discount_amount", "status"]).
 *
 * Dùng để admin audit ai sửa gì khi nào (compliance).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "VoucherAuditLog")
public class VoucherAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "voucher_id", nullable = false)
    private Long voucherId;

    @Column(name = "admin_id", nullable = false, length = 100)
    private String adminId;

    /** CREATE / UPDATE / DELETE / LOCK / UNLOCK / BULK_DELETE / BULK_LOCK / BULK_UNLOCK / BULK_SET_VIP. */
    @Column(nullable = false, length = 20)
    private String action;

    /** JSON snapshot giá trị cũ (NULL với CREATE). NVARCHAR(MAX). */
    @Column(name = "old_values", columnDefinition = "NVARCHAR(MAX)")
    private String oldValues;

    /** JSON snapshot giá trị mới (NULL với hard DELETE). NVARCHAR(MAX). */
    @Column(name = "new_values", columnDefinition = "NVARCHAR(MAX)")
    private String newValues;

    /** JSON array field đã đổi (vd: ["discount_amount","status"]). NVARCHAR(MAX). */
    @Column(name = "changed_fields", columnDefinition = "NVARCHAR(MAX)")
    private String changedFields;

    /** Ghi chú admin (vd: "Lock do hết số lượng"). */
    @Column(length = 500)
    private String note;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
