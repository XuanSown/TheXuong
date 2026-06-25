package com.example.thexuong.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO tạo mới Voucher catalog.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherCreateRequest {

    @Pattern(regexp = "^$|^TX-[A-HJ-NP-RT-Z0-9]{6}$",
             message = "Code phải định dạng TX-XXXXXX (không chứa 0/O/1/I/L)")
    private String code;

    @NotNull(message = "Mệnh giá không được để trống")
    @DecimalMin(value = "1000", message = "Mệnh giá tối thiểu 1,000đ")
    private BigDecimal discountAmount;

    @NotNull(message = "Điểm cần không được để trống")
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
