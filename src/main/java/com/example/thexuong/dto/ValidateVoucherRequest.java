package com.example.thexuong.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ValidateVoucherRequest {

    @NotBlank(message = "Ma voucher khong duoc de trong")
    private String code;

    @NotNull(message = "Tong tien khong duoc de trong")
    @Positive(message = "Tong tien phai lon hon 0")
    private BigDecimal total;
}
