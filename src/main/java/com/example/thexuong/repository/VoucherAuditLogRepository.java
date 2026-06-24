package com.example.thexuong.repository;

import com.example.thexuong.entity.VoucherAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository cho VoucherAuditLog. Hiện chỉ cần save (insert-only).
 * Read API có thể mở rộng sau (audit trail page).
 */
@Repository
public interface VoucherAuditLogRepository extends JpaRepository<VoucherAuditLog, Long> {
}
