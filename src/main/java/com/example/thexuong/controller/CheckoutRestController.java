package com.example.thexuong.controller;

import com.example.thexuong.dto.CartItemDto;
import com.example.thexuong.dto.CheckoutVoucherDto;
import com.example.thexuong.dto.ProductDto;
import com.example.thexuong.dto.UserResponse;
import com.example.thexuong.entity.Cart;
import com.example.thexuong.entity.UserVoucher;
import com.example.thexuong.entity.Voucher;
import com.example.thexuong.repository.UserVoucherRepository;
import com.example.thexuong.repository.VoucherRepository;
import com.example.thexuong.service.CartService;
import com.example.thexuong.service.PointService;
import com.example.thexuong.service.UserService;
import com.example.thexuong.service.VoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API for Checkout page data (Vue frontend consumption).
 * Provides combined data: cart, user profile, available vouchers, loyalty points.
 */
@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
@Slf4j
public class CheckoutRestController {

    private final CartService cartService;
    private final UserService userService;
    private final PointService pointService;
    private final UserVoucherRepository userVoucherRepository;
    private final VoucherRepository voucherRepository;
    private final com.example.thexuong.service.VoucherService voucherService;
    private final com.example.thexuong.repository.PointTierRepository pointTierRepository;

    /**
     * GET /api/checkout
     * Returns checkout page data:
     * - cart: items, total
     * - user: profile info
     * - currentPoints: available loyalty points
     * - availableVouchers: list of unused vouchers with discount info
     */
    @GetMapping
    public ResponseEntity<?> getCheckoutData(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Chưa đăng nhập"));
        }

        String email = authentication.getName();
        com.example.thexuong.entity.User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Không tìm thấy người dùng"));
        }

        // Get cart
        Cart cart = cartService.getCartByUser(email);
        double cartTotal = cart.getItems().stream()
                .mapToDouble(item -> {
                    var price = item.getProductVariant().getProduct().getPrice();
                    return (price != null ? price.doubleValue() : 0) * item.getQuantity();
                })
                .sum();

        // Convert cart items to DTO
        List<Map<String, Object>> cartItems = cart.getItems().stream()
                .map(item -> {
                    var variant = item.getProductVariant();
                    var product = variant.getProduct();
                    return Map.of(
                            "id", item.getId(),
                            "quantity", item.getQuantity(),
                            "variant", Map.of(
                                    "id", variant.getId(),
                                    "sku", variant.getSku(),
                                    "size", variant.getSize() != null ? variant.getSize().getName() : null
                            ),
                            "product", ProductDto.builder()
                                    .id(product.getId())
                                    .name(product.getName())
                                    .price(product.getPrice() != null ? product.getPrice().doubleValue() : null)
                                    .imageUrl(product.getImageUrl())
                                    .build()
                    );
                })
                .collect(Collectors.toList());

        // Get loyalty points
        int currentPoints = pointService.getCurrentPoints(user.getId());

        // Get user's unused vouchers with catalog info
        List<UserVoucher> unusedUserVouchers = userVoucherRepository.findByUserIdAndStatus(
                user.getId(), UserVoucher.Status.UNUSED);

        // Fetch all vouchers in one query to avoid lazy loading issues
        List<Long> voucherIds = unusedUserVouchers.stream()
                .map(UserVoucher::getVoucherId)
                .distinct()
                .toList();

        Map<Long, Voucher> voucherMap = voucherRepository.findAllById(voucherIds).stream()
                .collect(java.util.stream.Collectors.toMap(Voucher::getId, v -> v));

        List<CheckoutVoucherDto> checkoutVouchers = unusedUserVouchers.stream()
                .map(uv -> {
                    Voucher v = voucherMap.get(uv.getVoucherId());
                    if (v == null) return null;
                    return CheckoutVoucherDto.builder()
                            .userVoucherId(uv.getId())
                            .code(uv.getCode())
                            .discountAmount(v.getDiscountAmount())
                            .minOrderAmount(v.getMinOrderAmount())
                            .description("Giảm " + v.getDiscountAmount().intValue() + "đ")
                            .expiresAt(uv.getExpiresAt())
                            .build();
                })
                .filter(v -> v != null)
                .collect(Collectors.toList());

        // Get tier info
        String tierCode = user.getTierCode() != null ? user.getTierCode() : "THUONG";
        BigDecimal autoDiscountPercent = BigDecimal.ZERO;
        BigDecimal tierDiscountAmount = BigDecimal.ZERO;

        com.example.thexuong.entity.PointTier tier = pointTierRepository.findByCode(tierCode).orElse(null);
        if (tier != null && tier.getAutoDiscountPercent() != null && tier.getAutoDiscountPercent().compareTo(BigDecimal.ZERO) > 0) {
            autoDiscountPercent = tier.getAutoDiscountPercent();
            tierDiscountAmount = BigDecimal.valueOf(cartTotal)
                    .multiply(autoDiscountPercent)
                    .divide(new BigDecimal("100"), 0, java.math.RoundingMode.HALF_UP);
        }

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("cart", Map.of(
                "id", cart.getId(),
                "items", cartItems,
                "total", cartTotal,
                "itemCount", cart.getItems().size()
        ));
        response.put("user", UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .build());
        response.put("currentPoints", currentPoints);
        response.put("availableVouchers", checkoutVouchers);
        response.put("tierCode", tierCode);
        response.put("autoDiscountPercent", autoDiscountPercent);
        response.put("tierDiscountAmount", tierDiscountAmount);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/checkout/validate-voucher?code={code}&total={total}
     * Validates a voucher code and returns discount info
     */
    @GetMapping("/validate-voucher")
    public ResponseEntity<?> validateVoucher(
            @RequestParam String code,
            @RequestParam Double total,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Chưa đăng nhập"));
        }

        String email = authentication.getName();
        com.example.thexuong.entity.User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Không tìm thấy người dùng"));
        }

        try {
            BigDecimal discount = voucherService.validateAndGetDiscount(code, user.getId(), BigDecimal.valueOf(total));
            Map<String, Object> data = Map.of(
                    "code", code,
                    "discountAmount", discount.doubleValue(),
                    "finalTotal", total - discount.doubleValue()
            );
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Áp dụng voucher thành công",
                    "data", data
            ));
        } catch (Exception e) {
            log.error("Voucher validation failed for user {}: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Voucher không hợp lệ hoặc đã xảy ra lỗi."
            ));
        }
    }

    /**
     * GET /api/checkout/points
     * Returns current user's loyalty points balance
     */
    @GetMapping("/points")
    public ResponseEntity<?> getPoints(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Chưa đăng nhập"));
        }

        String email = authentication.getName();
        com.example.thexuong.entity.User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Không tìm thấy người dùng"));
        }

        int points = pointService.getCurrentPoints(user.getId());
        return ResponseEntity.ok(Map.of(
                "currentPoints", points
        ));
    }
}
