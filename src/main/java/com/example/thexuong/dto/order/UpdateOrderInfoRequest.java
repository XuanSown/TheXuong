package com.example.thexuong.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UpdateOrderInfoRequest {

    @NotBlank(message = "So dien thoai khong duoc de trong")
    @Pattern(regexp = "^0[0-9]{9,10}$", message = "So dien thoai khong hop le (bat dau 0, 10-11 so)")
    private String phoneNumber;

    @NotBlank(message = "Dia chi khong duoc de trong")
    private String address;
}
