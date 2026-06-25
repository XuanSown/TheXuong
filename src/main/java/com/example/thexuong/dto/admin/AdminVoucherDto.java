package com.example.thexuong.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Admin Voucher DTO matching frontend VoucherResponse.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminVoucherDto {
    private Long id;
    private String code;
    private BigDecimal discountAmount;
    private Integer requiredPoints;
    private BigDecimal minOrderAmount;
    private List<Integer> applicableCategoryIds;
    private List<Integer> applicableProductIds;
    private Boolean vipOnly;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long claimedCount;

    public static AdminVoucherDto fromEntity(com.example.thexuong.entity.Voucher voucher, long claimedCount) {
        List<Integer> categoryIds = parseJsonArray(voucher.getApplicableCategoryIds());
        List<Integer> productIds = parseJsonArray(voucher.getApplicableProductIds());

        return AdminVoucherDto.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .discountAmount(voucher.getDiscountAmount())
                .requiredPoints(voucher.getRequiredPoints())
                .minOrderAmount(voucher.getMinOrderAmount())
                .applicableCategoryIds(categoryIds)
                .applicableProductIds(productIds)
                .vipOnly(voucher.getVipOnly())
                .status(voucher.getStatus() != null ? voucher.getStatus().toString() : null)
                .createdAt(voucher.getCreatedAt())
                .updatedAt(voucher.getUpdatedAt())
                .claimedCount(claimedCount)
                .build();
    }

    private static List<Integer> parseJsonArray(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            json = json.trim();
            if (json.startsWith("[") && json.endsWith("]")) {
                String[] parts = json.substring(1, json.length() - 1).split(",");
                java.util.List<Integer> list = new java.util.ArrayList<>();
                for (String part : parts) {
                    try {
                        list.add(Integer.parseInt(part.trim()));
                    } catch (NumberFormatException e) {
                        // ignore invalid
                    }
                }
                return list;
            }
        } catch (Exception e) {
            // fall through
        }
        return null;
    }
}
