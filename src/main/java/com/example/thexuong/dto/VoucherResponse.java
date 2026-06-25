package com.example.thexuong.dto;

import com.example.thexuong.entity.Voucher;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO cho Voucher catalog.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
                .expiresAt(null)
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .claimedCount(claimedCount != null ? claimedCount : 0)
                .build();
    }

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
            return java.util.Arrays.stream(trimmed.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .toList();
        } catch (Exception e) {
            return null;
        }
    }
}
