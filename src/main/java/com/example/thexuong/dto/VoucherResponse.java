package com.example.thexuong.dto;

import com.example.thexuong.entity.Voucher;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Response DTO cho Voucher catalog.
 * Theo ADMIN_VOUCHER_REQUIREMENTS.md → VoucherResponse.
 *
 * - applicableCategoryIds / applicableProductIds: trả về List<Integer> (parse từ JSON string trong DB).
 * - claimedCount: COUNT(UserVoucher WHERE voucher_id = ?) — service tính riêng.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherResponse {
    private Long id;
    private String code;
    private BigDecimal discountAmount;
    private Integer requiredPoints;
    private BigDecimal minOrderAmount;
    private List<Integer> applicableCategoryIds;
    private List<Integer> applicableProductIds;
    private Boolean vipOnly;
    private String status;
    private LocalDate expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Integer claimedCount;

    /**
     * Map entity Voucher → VoucherResponse.
     * Parse JSON string "applicableCategoryIds"/"applicableProductIds" thành List<Integer>.
     * claimedCount set riêng bởi Service.
     */
    public static VoucherResponse from(Voucher v, Integer claimedCount) {
        return VoucherResponse.builder()
                .id(v.getId())
                .code(v.getCode())
                .discountAmount(v.getDiscountAmount())
                .requiredPoints(v.getRequiredPoints())
                .minOrderAmount(v.getMinOrderAmount())
                .applicableCategoryIds(parseIds(v.getApplicableCategoryIds()))
                .applicableProductIds(parseIds(v.getApplicableProductIds()))
                .vipOnly(v.getVipOnly())
                .status(v.getStatus() != null ? v.getStatus().name() : null)
                .expiresAt(null) // Voucher entity hiện chưa có field expiresAt — TODO thêm khi có DB migration
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .claimedCount(claimedCount != null ? claimedCount : 0)
                .build();
    }

    /**
     * Parse JSON array string "[1,2,3]" → List<Integer>.
     * Robust với null, empty, format không hợp lệ.
     */
    private static List<Integer> parseIds(String json) {
        if (json == null || json.isBlank() || "[]".equals(json.trim())) {
            return null;
        }
        try {
            String trimmed = json.trim();
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                trimmed = trimmed.substring(1, trimmed.length() - 1);
            }
            if (trimmed.isEmpty()) return null;
            return Arrays.stream(trimmed.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .toList();
        } catch (Exception e) {
            return null;
        }
    }
}
