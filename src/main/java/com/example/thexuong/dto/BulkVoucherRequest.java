package com.example.thexuong.dto;

import com.example.thexuong.enums.BulkAction;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request DTO cho POST /api/admin/loyalty/vouchers/bulk.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkVoucherRequest {

    @NotEmpty(message = "Danh sách ID không được rỗng")
    @Size(max = 100, message = "Tối đa 100 voucher mỗi lần bulk")
    private List<Long> ids;

    @NotNull(message = "Action không được để trống")
    private BulkAction action;

    private Boolean value;
    private String adminNote;
}
