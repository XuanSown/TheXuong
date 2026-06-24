package com.example.thexuong.controller.api;

import com.example.thexuong.dto.*;
import com.example.thexuong.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller quản lý Voucher catalog (admin).
 * Theo ADMIN_VOUCHER_REQUIREMENTS.md → API Endpoints.
 *
 * Prefix: /api/admin/loyalty/vouchers
 *
 * Tất cả endpoint yêu cầu quyền ADMIN hoặc BOTH.
 *
 * Endpoints:
 * - GET    /                          → danh sách voucher (paginated + filter)
 * - GET    /{id}                      → chi tiết
 * - POST   /                          → tạo mới
 * - PUT    /{id}                      → cập nhật (partial)
 * - DELETE /{id}                      → xóa (soft nếu có user claim)
 * - POST   /bulk                      → bulk action (LOCK/UNLOCK/DELETE/SET_VIP)
 * - GET    /stats                     → thống kê catalog
 *
 * @PreAuthorize ở class-level — không cần lặp lại trên từng method.
 */
@RestController
@RequestMapping("/api/admin/loyalty/vouchers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'BOTH')")
public class VoucherRestController {

    private final VoucherService voucherService;

    /**
     * GET /api/admin/loyalty/vouchers
     * Query params: page (1-based), size, search, status, vipOnly, minPoints, maxPoints, sortBy, sortDir
     */
    @GetMapping
    public ResponseEntity<ApiResponse<VoucherListResponse>> getVouchers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "5") Integer size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(required = false) Boolean vipOnly,
            @RequestParam(required = false) Integer minPoints,
            @RequestParam(required = false) Integer maxPoints,
            @RequestParam(defaultValue = "code") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        VoucherListResponse response = voucherService.getVouchers(
                page, size, search, status, vipOnly, minPoints, maxPoints, sortBy, sortDir
        );
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách voucher thành công.", response));
    }

    /**
     * GET /api/admin/loyalty/vouchers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VoucherResponse>> getVoucher(@PathVariable Long id) {
        VoucherResponse response = voucherService.getVoucher(id);
        return ResponseEntity.ok(ApiResponse.ok("Lấy thông tin voucher thành công.", response));
    }

    /**
     * POST /api/admin/loyalty/vouchers
     * Body: VoucherCreateRequest
     * Header: X-Admin-Username — lưu vào audit log
     */
    @PostMapping
    public ResponseEntity<ApiResponse<VoucherResponse>> createVoucher(
            @Valid @RequestBody VoucherCreateRequest request,
            @RequestHeader(value = "X-Admin-Username", required = false) String adminUsername
    ) {
        String username = adminUsername != null ? adminUsername : getCurrentUsername();
        VoucherResponse response = voucherService.createVoucher(request, username);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tạo voucher thành công.", response));
    }

    /**
     * PUT /api/admin/loyalty/vouchers/{id}
     * Body: VoucherUpdateRequest (partial — field null = giữ nguyên)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VoucherResponse>> updateVoucher(
            @PathVariable Long id,
            @Valid @RequestBody VoucherUpdateRequest request,
            @RequestHeader(value = "X-Admin-Username", required = false) String adminUsername
    ) {
        String username = adminUsername != null ? adminUsername : getCurrentUsername();
        VoucherResponse response = voucherService.updateVoucher(id, request, username);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật voucher thành công.", response));
    }

    /**
     * DELETE /api/admin/loyalty/vouchers/{id}
     * Soft delete (status → EXPIRED) nếu có user claim, ngược lại hard delete.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVoucher(
            @PathVariable Long id,
            @RequestHeader(value = "X-Admin-Username", required = false) String adminUsername
    ) {
        String username = adminUsername != null ? adminUsername : getCurrentUsername();
        voucherService.deleteVoucher(id, username);
        return ResponseEntity.ok(ApiResponse.ok("Đã xóa voucher thành công."));
    }

    /**
     * POST /api/admin/loyalty/vouchers/bulk
     * Body: BulkVoucherRequest { ids, action, value?, adminNote? }
     */
    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<BulkVoucherResponse>> bulkAction(
            @Valid @RequestBody BulkVoucherRequest request,
            @RequestHeader(value = "X-Admin-Username", required = false) String adminUsername
    ) {
        String username = adminUsername != null ? adminUsername : getCurrentUsername();
        BulkVoucherResponse response = voucherService.bulkAction(request, username);
        return ResponseEntity.ok(ApiResponse.ok(
                String.format("Bulk action hoàn tất: %d thành công, %d thất bại.",
                        response.getSuccessCount(), response.getFailureCount()),
                response
        ));
    }

    /**
     * GET /api/admin/loyalty/vouchers/stats
     * Trả về thống kê tổng quan catalog.
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<VoucherStats>> getStats() {
        VoucherStats stats = voucherService.getStats();
        return ResponseEntity.ok(ApiResponse.ok("Lấy thống kê voucher thành công.", stats));
    }

    /**
     * Lấy username của admin đang đăng nhập từ SecurityContext.
     * Fallback nếu frontend không gửi X-Admin-Username header.
     */
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "unknown";
    }
}
