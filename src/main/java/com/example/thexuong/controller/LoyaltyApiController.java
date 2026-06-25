package com.example.thexuong.controller;

import com.example.thexuong.entity.User;
import com.example.thexuong.entity.UserVoucher;
import com.example.thexuong.repository.UserRepository;
import com.example.thexuong.service.PointService;
import com.example.thexuong.service.VoucherService;
import com.example.thexuong.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API cho loyalty (dùng cho AJAX + Vue frontend tương lai).
 *
 * Endpoints (all under base /api/v1):
 * - GET  /loyalty/validate-voucher?code=X&total=Y : validate + trả discount
 * - GET  /loyalty/points : số dư điểm hiện tại
 * - GET  /loyalty/history : lịch sử giao dịch
 * - GET  /my-vouchers : voucher của tôi (cả 3 status)
 * - GET  /my-vouchers?status=UNUSED : filter theo status
 *
 * Tất cả endpoints yêu cầu user đăng nhập (session-based auth).
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LoyaltyApiController {

    private final VoucherService voucherService;
    private final PointService pointService;
    private final UserRepository userRepository;

    @GetMapping("/loyalty/validate-voucher")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateVoucher(
            @RequestParam("code") String code,
            @RequestParam("total") BigDecimal total,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập."));
        }
        User user = resolveUser(principal);
        try {
            BigDecimal discount = voucherService.validateAndGetDiscount(code, user.getId(), total);
            Map<String, Object> data = new HashMap<>();
            data.put("code", code);
            data.put("discountAmount", discount);
            data.put("finalTotal", total.subtract(discount));
            return ResponseEntity.ok(ApiResponse.ok("Áp dụng thành công.", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/loyalty/points")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPoints(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập."));
        }
        User user = resolveUser(principal);
        int points = pointService.getCurrentPoints(user.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("currentPoints", points);
        return ResponseEntity.ok(ApiResponse.ok("OK", data));
    }

    @GetMapping("/loyalty/history")
    public ResponseEntity<ApiResponse<List<?>>> getHistory(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập."));
        }
        User user = resolveUser(principal);
        return ResponseEntity.ok(ApiResponse.ok("OK", pointService.getHistory(user.getId())));
    }

    @GetMapping("/my-vouchers")
    public ResponseEntity<ApiResponse<List<UserVoucher>>> getMyVouchers(
            @RequestParam(value = "status", required = false) UserVoucher.Status status,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập."));
        }
        User user = resolveUser(principal);
        List<UserVoucher> vouchers = (status == null)
                ? voucherService.getUserVouchers(user.getId())
                : voucherService.getUserVouchersByStatus(user.getId(), status);
        return ResponseEntity.ok(ApiResponse.ok("OK", vouchers));
    }

    private User resolveUser(Principal principal) {
        String identifier = principal.getName();
        return userRepository.findByEmail(identifier)
                .orElseGet(() -> userRepository.findByUsername(identifier).orElse(null));
    }
}
