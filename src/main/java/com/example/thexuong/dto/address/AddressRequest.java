package com.example.thexuong.dto.address;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AddressRequest {
    @Size(max = 50, message = "Nhãn tối đa 50 ký tự")
    private String label;

    @NotBlank(message = "Tên người nhận không được để trống")
    @Size(max = 255, message = "Tên người nhận tối đa 255 ký tự")
    private String recipientName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0[0-9]{9,10}$", message = "Số điện thoại không hợp lệ (bắt đầu 0, 10-11 số)")
    private String recipientPhone;

    @NotBlank(message = "Vui lòng chọn tỉnh/thành phố")
    private String provinceCode;
    @NotBlank(message = "Vui lòng chọn quận/huyện")
    private String districtCode;
    @NotBlank(message = "Vui lòng chọn phường/xã")
    private String wardCode;

    @Size(max = 255, message = "Số nhà/đường tối đa 255 ký tự")
    private String streetDetail;

    private Double latitude;
    private Double longitude;

    private Boolean isDefault = false;
}
