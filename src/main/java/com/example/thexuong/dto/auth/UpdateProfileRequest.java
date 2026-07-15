package com.example.thexuong.dto.auth;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {
    @Size(max = 255, message = "Họ tên tối đa 255 ký tự")
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    @Pattern(regexp = "^0[0-9]{9,10}$", message = "Số điện thoại không hợp lệ (bắt đầu 0, 10-11 số)")
    private String phoneNumber;

    @Size(max = 500, message = "Địa chỉ tối đa 500 ký tự")
    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;
}
