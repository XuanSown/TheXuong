package com.example.thexuong.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UpdateOrderStatusRequest {

    @NotBlank(message = "Trang thai khong duoc de trong")
    @Pattern(regexp = "PENDING|CONFIRMED|SHIPPING|DELIVERED|COMPLETED|CANCELLED|REFUNDED",
             message = "Trang thai khong hop le")
    private String status;
}
