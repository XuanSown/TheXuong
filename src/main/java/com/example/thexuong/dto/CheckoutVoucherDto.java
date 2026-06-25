package com.example.thexuong.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Simplified Voucher DTO for checkout page.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutVoucherDto {
    private Long userVoucherId;
    private String code;
    private BigDecimal discountAmount;
    private BigDecimal minOrderAmount;
    private String description;
    private LocalDateTime expiresAt;
}
