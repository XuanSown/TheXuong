package com.example.thexuong.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO cho bulk operations.
 * Theo ADMIN_VOUCHER_REQUIREMENTS.md → BulkVoucherResponse.
 *
 * Trả về danh sách các voucher FAIL để admin biết cần xử lý thủ công.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkVoucherResponse {
    private int totalRequested;
    private int successCount;
    private int failureCount;
    private List<BulkResult> failures;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkResult {
        private Long id;
        private String error;
    }
}
