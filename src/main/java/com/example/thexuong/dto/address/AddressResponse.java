package com.example.thexuong.dto.address;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressResponse {
    private Long id;
    private String label;
    private String recipientName;
    private String recipientPhone;
    private String provinceCode;
    private String districtCode;
    private String wardCode;
    private String streetDetail;
    private Double latitude;
    private Double longitude;
    private Boolean isDefault;
}
