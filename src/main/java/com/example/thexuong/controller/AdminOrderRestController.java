package com.example.thexuong.controller;

import com.example.thexuong.dto.admin.UpdateOrderStatusRequest;
import com.example.thexuong.entity.Order;
import com.example.thexuong.entity.OrderStatus;
import com.example.thexuong.repository.OrderRepository;
import com.example.thexuong.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Admin REST API for Order Management.
 * Base path: /api/v1/admin/orders
 */
@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'BOTH')")
public class AdminOrderRestController {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    /**
     * GET /api/v1/admin/orders
     * Query params: status, keyword, page, size
     * Uses JPA Specification for DB-level filtering + pagination.
     */
    @GetMapping
    public ResponseEntity<?> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        // Build Specification for DB-level filtering
        Specification<Order> spec = (root, query, cb) -> cb.conjunction();

        if (status != null && !status.isEmpty()) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status);
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("status"), orderStatus));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid status: " + status));
            }
        }

        if (keyword != null && !keyword.isBlank()) {
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> {
                // Search in fullName, phoneNumber, id (as string)
                return cb.or(
                        cb.like(cb.lower(root.get("fullName")), kw),
                        cb.like(root.get("phoneNumber"), kw),
                        cb.like(cb.lower(root.get("id").as(String.class)), kw.replace("%", ""))
                );
            });
        }

        // Sort by id DESC (newest first)
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Order> orderPage = orderRepository.findAll(spec, pageable);

        List<Map<String, Object>> orderList = orderPage.getContent().stream()
                .map(this::toOrderSummary)
                .toList();

        Map<String, Object> response = Map.of(
                "content", orderList,
                "totalElements", orderPage.getTotalElements(),
                "totalPages", orderPage.getTotalPages(),
                "size", orderPage.getSize(),
                "number", orderPage.getNumber()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/v1/admin/orders/{id}/status
     * Body: { status: "CONFIRMED" | "SHIPPING" | "DELIVERED" | "COMPLETED" | "CANCELLED" | "REFUNDED" }
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        String newStatusStr = request.getStatus();
        try {
            OrderStatus newStatus = OrderStatus.valueOf(newStatusStr);
            Order updatedOrder = orderService.adminUpdateStatus(id, newStatus);

            return ResponseEntity.ok(Map.of(
                    "message", "Cập nhật trạng thái thành công",
                    "order", toOrderSummary(updatedOrder)
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid status: " + newStatusStr));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========== Helper Methods ==========

    private Map<String, Object> toOrderSummary(Order order) {
        BigDecimal totalMoney = order.getTotalMoney();
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", order.getId());
        map.put("fullName", order.getFullName());
        map.put("phoneNumber", order.getPhoneNumber());
        map.put("totalMoney", totalMoney != null ? totalMoney : BigDecimal.ZERO);
        map.put("status", order.getStatus() != null ? order.getStatus().toString() : null);
        map.put("paymentMethod", order.getPaymentMethod());
        map.put("createdAt", order.getCreatedAt());
        map.put("pointsUsed", order.getPointsUsed() != null ? order.getPointsUsed() : 0);
        map.put("voucherCode", order.getVoucherCode());
        map.put("discountAmount", order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO);
        map.put("address", order.getAddress());
        map.put("shippingFee", order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO);
        map.put("subtotal", order.getSubtotal() != null ? order.getSubtotal() : BigDecimal.ZERO);
        return map;
    }
}
