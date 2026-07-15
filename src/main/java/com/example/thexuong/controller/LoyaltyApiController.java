package com.example.thexuong.controller;

import com.example.thexuong.dto.ValidateVoucherRequest;
import com.example.thexuong.entity.User;
import com.example.thexuong.entity.UserVoucher;
import com.example.thexuong.exception.PointBalanceException;
import com.example.thexuong.exception.VoucherInvalidException;
import com.example.thexuong.repository.UserRepository;
import com.example.thexuong.service.PointService;
import com.example.thexuong.service.VoucherService;
import com.example.thexuong.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class LoyaltyApiController {

    private final VoucherService voucherService;
    private final PointService pointService;
    private final UserRepository userRepository;

    @GetMapping("/loyalty/validate-voucher")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateVoucher(
            @Valid @ModelAttribute ValidateVoucherRequest request,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập."));
        }
        User user = resolveUser(principal);
        try {
            BigDecimal discount = voucherService.validateAndGetDiscount(request.getCode(), user.getId(), request.getTotal());
            Map<String, Object> data = new HashMap<>();
            data.put("code", request.getCode());
            data.put("discountAmount", discount);
            data.put("finalTotal", request.getTotal().subtract(discount));
            return ResponseEntity.ok(ApiResponse.ok("Áp dụng thành công.", data));
        } catch (VoucherInvalidException e) {
            log.warn("Voucher validation failed for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error validating voucher for user {}: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Voucher không hợp lệ hoặc đã xảy ra lỗi."));
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

    @GetMapping("/loyalty/catalog")
    public ResponseEntity<ApiResponse<List<com.example.thexuong.entity.Voucher>>> getCatalog() {
        return ResponseEntity.ok(ApiResponse.ok("OK", voucherService.getActiveCatalog()));
    }

    @PostMapping("/loyalty/redeem")
    public ResponseEntity<ApiResponse<UserVoucher>> redeemVoucher(
            @RequestBody Map<String, Object> payload,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập."));
        }
        User user = resolveUser(principal);
        Object rawVoucherId = payload.get("voucherId");
        if (rawVoucherId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Thiếu voucherId."));
        }
        try {
            Long voucherId = Long.valueOf(rawVoucherId.toString());
            UserVoucher uv = voucherService.redeemVoucher(user.getId(), voucherId);
            return ResponseEntity.ok(ApiResponse.ok("Đổi voucher thành công.", uv));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("voucherId không hợp lệ."));
        } catch (VoucherInvalidException | PointBalanceException e) {
            log.warn("Lỗi khi đổi voucher cho user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error redeeming voucher for user {}: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Đã xảy ra lỗi khi đổi voucher."));
        }
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
