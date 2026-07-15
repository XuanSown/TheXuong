package com.example.thexuong.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for placing an order (checkout).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceOrderRequest {
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0[0-9]{9,10}$", message = "Số điện thoại không hợp lệ (bắt đầu 0, 10-11 số)")
    private String phoneNumber;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    @Builder.Default
private String paymentMethod = "COD";

    private String voucherCode;

    @Min(value = 0, message = "Điểm sử dụng không được âm")
    private Integer pointsToUse;
}
