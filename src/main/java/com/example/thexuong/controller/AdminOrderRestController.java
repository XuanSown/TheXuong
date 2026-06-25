package com.example.thexuong.controller;

import com.example.thexuong.entity.Order;
import com.example.thexuong.entity.OrderStatus;
import com.example.thexuong.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin REST API for Order Management.
 * Base path: /api/v1/admin/orders
 */
@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
public class AdminOrderRestController {

    private final OrderRepository orderRepository;

    /**
     * GET /api/admin/orders
     * Query params: status, page, size
     */
    @GetMapping
    public ResponseEntity<?> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<Order> orders;
        if (status != null && !status.isEmpty()) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status);
                orders = orderRepository.findAll().stream()
                        .filter(o -> o.getStatus() == orderStatus)
                        .toList();
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid status: " + status));
            }
        } else {
            orders = orderRepository.findAll();
        }

        // Pagination manual since we're using stream
        int start = page * size;
        int end = Math.min(start + size, orders.size());
        List<Order> pagedOrders = start < orders.size() ? orders.subList(start, end) : List.of();

        List<Map<String, Object>> orderList = pagedOrders.stream()
                .map(this::toOrderSummary)
                .collect(Collectors.toList());

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("content", orderList);
        response.put("totalElements", (long) orders.size());
        response.put("totalPages", (int) Math.ceil((double) orders.size() / size));
        response.put("size", size);
        response.put("number", page);

        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/admin/orders/{id}/status
     * Body: { status: "CONFIRMED" | "SHIPPING" | "DELIVERED" | "COMPLETED" | "CANCELLED" | "REFUNDED" }
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String newStatusStr = body.get("status");
        if (newStatusStr == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing status field"));
        }

        try {
            OrderStatus newStatus = OrderStatus.valueOf(newStatusStr);
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + id));

            OrderStatus current = order.getStatus();
            if (!current.canTransitionTo(newStatus)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Không thể chuyển từ " + current + " sang " + newStatus
                ));
            }

            order.setStatus(newStatus);
            LocalDateTime now = LocalDateTime.now();
            switch (newStatus) {
                case CONFIRMED -> order.setPaidAt(now);
                case SHIPPING -> order.setShippedAt(now);
                case DELIVERED -> order.setDeliveredAt(now);
                case COMPLETED -> order.setCompletedAt(now);
                case CANCELLED -> order.setCancelledAt(now);
                case REFUNDED -> order.setRefundedAt(now);
                default -> {}
            }
            orderRepository.save(order);

            return ResponseEntity.ok(Map.of(
                    "message", "Cập nhật trạng thái thành công",
                    "order", toOrderSummary(order)
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid status: " + newStatusStr));
        }
    }

    // ========== Helper Methods ==========

    private Map<String, Object> toOrderSummary(Order order) {
        return new java.util.HashMap<String, Object>() {{
            put("id", order.getId());
            put("fullName", order.getFullName());
            put("phoneNumber", order.getPhoneNumber());
            put("totalMoney", order.getTotalMoney());
            put("status", order.getStatus() != null ? order.getStatus().toString() : null);
            put("paymentMethod", order.getPaymentMethod());
            put("createdAt", order.getCreatedAt());
            put("pointsUsed", order.getPointsUsed());
            put("voucherCode", order.getVoucherCode());
            put("discountAmount", order.getDiscountAmount());
        }};
    }
}
