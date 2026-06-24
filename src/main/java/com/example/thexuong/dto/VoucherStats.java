package com.example.thexuong.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO cho GET /api/admin/loyalty/vouchers/stats.
 * Theo ADMIN_VOUCHER_REQUIREMENTS.md → VoucherStatsResponse.
 *
 * Snapshot thống kê toàn bộ catalog.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherStats {
    private int totalVouchers;
    private int activeVouchers;
    private int lockedVouchers;
    private int expiredVouchers;
    private int vipVouchers;
    private int totalClaimed;
    private Map<String, Integer> byStatus;
}
