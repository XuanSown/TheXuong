package com.example.thexuong.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Response DTO cho GET /api/admin/loyalty/vouchers/stats.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherStats {
    private int totalVouchers;
    private int activeVouchers;
    private int lockedVouchers;
    private int expiredVouchers;
    private int vipVouchers;
    private int totalClaimed;
    private Map<String, Integer> byStatus;
}
