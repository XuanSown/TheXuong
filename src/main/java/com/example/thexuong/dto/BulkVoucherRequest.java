package com.example.thexuong.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO cho POST /api/admin/loyalty/vouchers/bulk.
 * Theo ADMIN_VOUCHER_REQUIREMENTS.md → BulkVoucherRequest.
 *
 * Actions: "LOCK", "UNLOCK", "DELETE", "SET_VIP".
 * - LOCK/UNLOCK: set status ACTIVE ↔ LOCKED
 * - DELETE: soft delete (status → EXPIRED). Không xoá row vật lý.
 * - SET_VIP: cần thêm field `value` (true/false).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkVoucherRequest {

    @NotEmpty(message = "Danh sách ID không được rỗng")
    @Size(max = 100, message = "Tối đa 100 voucher mỗi lần bulk")
    private List<Long> ids;

    @NotNull(message = "Action không được để trống")
    private String action;

    /** Required cho action = SET_VIP. Bỏ qua cho các action khác. */
    private Boolean value;

    /** Ghi chú admin (lưu audit log cho mỗi voucher). Optional. */
    private String adminNote;
}
