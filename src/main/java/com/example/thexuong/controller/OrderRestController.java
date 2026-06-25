package com.example.thexuong.controller;

import com.example.thexuong.dto.OrderDto;
import com.example.thexuong.dto.OrderItemDto;
import com.example.thexuong.dto.order.PlaceOrderRequest;
import com.example.thexuong.entity.Order;
import com.example.thexuong.repository.OrderRepository;
import com.example.thexuong.repository.UserRepository;
import com.example.thexuong.service.OrderService;
import com.example.thexuong.service.VNPayService;
import com.example.thexuong.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class OrderRestController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final VNPayService vnPayService;
    private final VoucherService voucherService;

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
                    System.err.println("[VOUCHER] Failed to mark as used: " + e.getMessage());
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
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
            Order order = orderService.getOrderByIdAndUser(id, authentication.getName());
            return ResponseEntity.ok(toOrderDto(order));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/orders/{id}/update-info
     * Update shipping info (phone, address)
     */
    @PutMapping("/{id}/update-info")
    public ResponseEntity<?> updateOrderInfo(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Chưa đăng nhập"));
        }

        try {
            String phoneNumber = body.get("phoneNumber");
            String address = body.get("address");

            orderService.updateOrderInfo(id, phoneNumber, address, authentication.getName());
            return ResponseEntity.ok(Map.of("message", "Cập nhật thông tin thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/orders/{id}/cancel
     * Cancel order (only PENDING)
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Long id,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Chưa đăng nhập"));
        }

        try {
            orderService.cancelOrder(id, authentication.getName());
            return ResponseEntity.ok(Map.of("message", "Đã hủy đơn hàng thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/orders/{id}/confirm-received
     * User confirms received → DELIVERED → COMPLETED
     */
    @PostMapping("/{id}/confirm-received")
    public ResponseEntity<?> confirmReceived(
            @PathVariable Long id,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Chưa đăng nhập"));
        }

        try {
            orderService.confirmReceived(id, authentication.getName());
            return ResponseEntity.ok(Map.of("message",
                    "Cảm ơn anh/chị đã xác nhận nhận hàng! Đơn hàng đã hoàn tất."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ========== Helper Methods ==========

    private OrderDto toOrderDto(Order order) {
        List<OrderItemDto> items = order.getOrderDetails().stream()
                .map(d -> OrderItemDto.builder()
                        .id(d.getId())
                        .productName(d.getProductName())
                        .size(d.getSize())
                        .price(d.getPrice())
                        .quantity(d.getQuantity())
                        .totalPrice(d.getTotalPrice())
                        .build())
                .collect(Collectors.toList());

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
