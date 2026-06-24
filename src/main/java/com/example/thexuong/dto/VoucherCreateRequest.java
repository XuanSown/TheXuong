package com.example.thexuong.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO tạo mới Voucher catalog.
 * Theo ADMIN_VOUCHER_REQUIREMENTS.md → API Endpoints → VoucherCreateRequest.
 *
 * Validation:
 * - code: optional (null/empty → auto-generate TX- + 6 random)
 * - discountAmount: required, in [10k,20k,50k,100k,200k,500k]
 * - requiredPoints: required, 1..50
 * - minOrderAmount: optional (default 0)
 * - vipOnly: optional (default false)
 * - status: optional (default "ACTIVE")
 * - expiresAt: optional (null = never)
 * - applicableCategoryIds / applicableProductIds: optional JSON array as List<Integer>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherCreateRequest {

    /** Mã catalog. Format: TX-[A-HJ-NP-RT-Z0-9]{6}. Null/empty → auto-generate. */
    @Pattern(regexp = "^$|^TX-[A-HJ-NP-RT-Z0-9]{6}$",
             message = "Code phải định dạng TX-XXXXXX (không chứa 0/O/1/I/L)")
    private String code;

    @NotNull(message = "Mệnh giá không được để trống")
    @DecimalMin(value = "1000", message = "Mệnh giá tối thiểu 1,000đ")
    private BigDecimal discountAmount;

    @NotNull(message = "Điểm cần không được để trống")
    @Min(value = 1, message = "Điểm cần tối thiểu 1")
    private Integer requiredPoints;

    /** Đơn tối thiểu. Default 0. Phải >= discountAmount (validator kiểm tra). */
    @DecimalMin(value = "0", message = "Min order không được âm")
    private BigDecimal minOrderAmount;

    /** JSON array category IDs. Null = áp dụng tất cả. */
    private List<Integer> applicableCategoryIds;

    /** JSON array product IDs. Null = áp dụng tất cả. */
    private List<Integer> applicableProductIds;

    /** Chỉ VIP mới đổi được. Default false. */
    private Boolean vipOnly;

    /** ACTIVE / LOCKED / EXPIRED. Default ACTIVE. */
    private String status;

    /** Admin-set expiration. Null = không bao giờ expire. */
    private LocalDate expiresAt;

    /** Ghi chú admin (lưu audit log). Optional. */
    private String adminNote;
}
