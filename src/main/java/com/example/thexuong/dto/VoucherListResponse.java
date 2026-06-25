package com.example.thexuong.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response DTO cho GET /api/admin/loyalty/vouchers (paginated list).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherListResponse {
    private List<VoucherResponse> vouchers;
    private long total;
    private int page;
    private int size;
    private int totalPages;
}
