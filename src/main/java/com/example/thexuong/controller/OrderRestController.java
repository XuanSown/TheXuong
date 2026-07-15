package com.example.thexuong.controller;

import com.example.thexuong.dto.OrderDto;
import com.example.thexuong.dto.OrderItemDto;
import com.example.thexuong.dto.order.PlaceOrderRequest;
import com.example.thexuong.dto.order.UpdateOrderInfoRequest;
import com.example.thexuong.entity.Order;
import com.example.thexuong.repository.OrderRepository;
import com.example.thexuong.repository.UserRepository;
import com.example.thexuong.service.OrderService;
import com.example.thexuong.service.VNPayService;
import com.example.thexuong.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API for Orders (Vue frontend consumption).
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderRestController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final VNPayService vnPayService;
    private final VoucherService voucherService;
    private final com.example.thexuong.repository.ProductRepository productRepository;

    /**
     * POST /api/orders
     * Create a new order from cart
     */
    @PostMapping
    public ResponseEntity<?> createOrder(
        Authentication authentication,
        @Valid @RequestBody PlaceOrderRequest request) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Chưa đăng nhập"));
        }

        try {
            String username = authentication.getName();
            Order order = orderService.placeOrder(
                username,
                request.getFullName(),
                request.getPhoneNumber(),
                request.getAddress(),
                request.getVoucherCode(),
                request.getPointsToUse()
            );

            // Set payment method
            order.setPaymentMethod(request.getPaymentMethod());
            orderRepository.save(order);

            // Mark voucher as used if applied
            if (request.getVoucherCode() != null && !request.getVoucherCode().isBlank()) {
                try {
                    voucherService.markAsUsed(request.getVoucherCode(), order.getId());
                } catch (Exception e) {
                    log.error("[VOUCHER] Failed to mark as used for order {}: {}", order.getId(), e.getMessage(), e);
                }
            }

            OrderDto orderDto = toOrderDto(order);

            // If VNPAY, return payment URL
            if ("VNPAY".equalsIgnoreCase(request.getPaymentMethod())) {
                String orderInfo = request.getVoucherCode() != null && !request.getVoucherCode().isBlank()
                    ? "Thanh toan don hang ma so " + order.getId() + " voucher=" + request.getVoucherCode().trim()
                    : "Thanh toan don hang ma so " + order.getId();
                String vnpayUrl = vnPayService.createOrder(order.getTotalMoney().intValue(), orderInfo, null);
                orderDto.setPaymentUrl(vnpayUrl);
            }

            return ResponseEntity.ok(Map.of(
                "message", "Đặt hàng thành công",
                "order", orderDto
            ));

        } catch (Exception e) {
            log.error("Failed to create order for user {}: {}", authentication.getName(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Đã xảy ra lỗi khi đặt hàng. Vui lòng thử lại sau."));
        }
    }

    /**
     * GET /api/orders
     * List current user's orders
     */
    @GetMapping
    public ResponseEntity<?> getMyOrders(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Chưa đăng nhập"));
        }

        String identifier = authentication.getName();
        Long userId = userRepository.findByEmail(identifier)
            .map(u -> u.getId())
            .orElseGet(() -> userRepository.findByUsername(identifier)
                .map(u -> u.getId())
                .orElse(null));

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Không tìm thấy người dùng"));
        }

        List<Order> orders = orderRepository.findByUserIdWithDetails(userId);
        List<OrderDto> orderDtos = orders.stream()
            .map(this::toOrderDto)
            .collect(Collectors.toList());

        return ResponseEntity.ok(orderDtos);
    }

    /**
     * GET /api/orders/{id}
     * Get order detail
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Chưa đăng nhập"));
        }

        try {
            Long userId = resolveUserId(authentication.getName());
            boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("BOTH"));
            
            Order order;
            if (isAdmin) {
                order = orderRepository.findByIdWithDetails(id).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
            } else {
                order = orderService.getOrderByIdAndUser(id, userId);
            }
            return ResponseEntity.ok(toOrderDto(order));
        } catch (Exception e) {
            log.error("Failed to get order {} for user {}: {}", id, authentication.getName(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Không tìm thấy đơn hàng."));
        }
    }

    /**
     * PUT /api/orders/{id}/update-info
     * Update shipping info (phone, address)
     */
    @PutMapping("/{id}/update-info")
    public ResponseEntity<?> updateOrderInfo(
        @PathVariable Long id,
        @Valid @RequestBody UpdateOrderInfoRequest request,
        Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Chưa đăng nhập"));
        }

        try {
            Long userId = resolveUserId(authentication.getName());
            orderService.updateOrderInfo(id, request.getPhoneNumber(), request.getAddress(), userId);
            return ResponseEntity.ok(Map.of("message", "Cập nhật thông tin thành công"));
        } catch (Exception e) {
            log.error("Failed to update order {} info: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Đã xảy ra lỗi khi cập nhật thông tin đơn hàng."));
        }
    }

    /**
     * POST /api/orders/{id}/cancel
     * Cancel order (only PENDING)
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id, Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Chưa đăng nhập"));
        }

        try {
            Long userId = resolveUserId(authentication.getName());
            orderService.cancelOrder(id, userId);
            return ResponseEntity.ok(Map.of("message", "Đã hủy đơn hàng thành công"));
        } catch (Exception e) {
            log.error("Failed to cancel order {} for user {}: {}", id, authentication.getName(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Lỗi không xác định"));
        }
    }

    /**
     * POST /api/orders/{id}/confirm-received
     * User confirms received → DELIVERED → COMPLETED
     */
    @PostMapping("/{id}/confirm-received")
    public ResponseEntity<?> confirmReceived(@PathVariable Long id, Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Chưa đăng nhập"));
        }

        try {
            Long userId = resolveUserId(authentication.getName());
            orderService.confirmReceived(id, userId);
            return ResponseEntity.ok(Map.of(
                "message", "Cảm ơn anh/chị đã xác nhận nhận hàng! Đơn hàng đã hoàn tất."));
        } catch (Exception e) {
            log.error("Failed to confirm order {} for user {}: {}", id, authentication.getName(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Đã xảy ra lỗi khi xác nhận nhận hàng."));
        }
    }

    // ========== Helper Methods ==========

    /**
     * Resolve userId từ identifier (email hoặc username) trong SecurityContext.
     */
    private Long resolveUserId(String identifier) {
        return userRepository.findByEmail(identifier)
            .map(u -> u.getId())
            .orElseGet(() -> userRepository.findByUsername(identifier)
                .map(u -> u.getId())
                .orElse(null));
    }

    private OrderDto toOrderDto(Order order) {
        List<OrderItemDto> items = new java.util.ArrayList<>();
        if (order.getOrderDetails() != null) {
            items = order.getOrderDetails().stream()
                .map(d -> {
                    String imageUrl = null;
                    if (d.getProductId() != null) {
                        imageUrl = productRepository.findById(d.getProductId())
                                .map(com.example.thexuong.entity.Product::getImageUrl)
                                .orElse(null);
                    }
                    return OrderItemDto.builder()
                        .id(d.getId())
                        .productName(d.getProductName())
                        .size(d.getSize())
                        .price(d.getPrice())
                        .quantity(d.getQuantity())
                        .totalPrice(d.getTotalPrice())
                        .imageUrl(imageUrl)
                        .build();
                })
                .collect(Collectors.toList());
        }

        return OrderDto.builder()
            .id(order.getId())
            .fullName(order.getFullName())
            .phoneNumber(order.getPhoneNumber())
            .address(order.getAddress())
            .paymentMethod(order.getPaymentMethod())
            .subtotal(order.getSubtotal())
            .shippingFee(order.getShippingFee())
            .discountAmount(order.getDiscountAmount())
            .pointsUsed(order.getPointsUsed())
            .voucherCode(order.getVoucherCode())
            .totalMoney(order.getTotalMoney())
            .status(order.getStatus() != null ? order.getStatus().toString() : null)
            .paidAt(order.getPaidAt())
            .shippedAt(order.getShippedAt())
            .deliveredAt(order.getDeliveredAt())
            .completedAt(order.getCompletedAt())
            .cancelledAt(order.getCancelledAt())
            .refundedAt(order.getRefundedAt())
            .createdAt(order.getCreatedAt())
            .items(items)
            .build();
    }
}
