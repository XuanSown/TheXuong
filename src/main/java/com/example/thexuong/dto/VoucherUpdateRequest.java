package com.example.thexuong.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO cập nhật Voucher catalog.
 * Theo ADMIN_VOUCHER_REQUIREMENTS.md → VoucherUpdateRequest (partial update).
 *
 * Tất cả field optional. Field null = giữ nguyên giá trị cũ.
 * KHÔNG cho update code (theo requirement "code không cho sửa nếu đã có UserVoucher claim").
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherUpdateRequest {

    @DecimalMin(value = "1000", message = "Mệnh giá tối thiểu 1,000đ")
    private BigDecimal discountAmount;

    @Min(value = 1, message = "Điểm cần tối thiểu 1")
    private Integer requiredPoints;

    @DecimalMin(value = "0", message = "Min order không được âm")
    private BigDecimal minOrderAmount;

    /** null = giữ nguyên, [] = xoá hết applicable categories. */
    private List<Integer> applicableCategoryIds;

    /** null = giữ nguyên, [] = xoá hết applicable products. */
    private List<Integer> applicableProductIds;

    /** null = giữ nguyên. */
    private Boolean vipOnly;

    /** null = giữ nguyên. ACTIVE / LOCKED / EXPIRED. */
    private String status;

    /** null = giữ nguyên (không set expiration). LocalDate cũ. */
    private LocalDate expiresAt;

    /** Ghi chú admin (lưu audit log). Bắt buộc nếu status → LOCKED/EXPIRED (validator kiểm tra). */
    private String adminNote;
}
