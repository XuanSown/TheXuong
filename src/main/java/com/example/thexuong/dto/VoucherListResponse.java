package com.example.thexuong.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO cho GET /api/admin/loyalty/vouchers (paginated list).
 * Theo ADMIN_VOUCHER_REQUIREMENTS.md → Response của READ.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherListResponse {
    private List<VoucherResponse> vouchers;
    private long total;
    private int page;
    private int size;
    private int totalPages;
}
