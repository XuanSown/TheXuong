package com.example.thexuong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Audit log cho mọi thay đổi Voucher catalog.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Column(nullable = false, length = 20)
    private String action;

    @Column(name = "old_values", columnDefinition = "NVARCHAR(MAX)")
    private String oldValues;

    @Column(name = "new_values", columnDefinition = "NVARCHAR(MAX)")
    private String newValues;

    @Column(name = "changed_fields", columnDefinition = "NVARCHAR(MAX)")
    private String changedFields;

    @Column(length = 500)
    private String note;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
