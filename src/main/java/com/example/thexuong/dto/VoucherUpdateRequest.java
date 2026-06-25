package com.example.thexuong.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO cập nhật Voucher catalog.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherUpdateRequest {

    @DecimalMin(value = "1000", message = "Mệnh giá tối thiểu 1,000đ")
    private BigDecimal discountAmount;

    @Min(value = 1, message = "Điểm cần tối thiểu 1")
    private Integer requiredPoints;

    @DecimalMin(value = "0", message = "Min order không được âm")
    private BigDecimal minOrderAmount;

    private List<Integer> applicableCategoryIds;
    private List<Integer> applicableProductIds;
    private Boolean vipOnly;
    private String status;
    private LocalDate expiresAt;
    private String adminNote;
}
