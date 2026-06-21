package com.example.thexuong.service;

import com.example.thexuong.entity.*;
import com.example.thexuong.exception.IllegalOrderTransitionException;
import com.example.thexuong.repository.OrderDetailRepository;
import com.example.thexuong.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    @Autowired
    private final OrderRepository orderRepository;
    @Autowired
    private final CartService cartService;
    @Autowired
    private final OrderDetailRepository orderDetailRepository;

    @Transactional
    public Order placeOrder(String username, String fullName, String phone, String address) {
        Cart cart = cartService.getCartByUser(username);
        List<CartItem> cartItems = cart.getItems();

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống");
        }

        // 1. Tính tổng tiền
        BigDecimal totalMoney = cartItems.stream()
                .map(item -> item.getProductVariant().getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Tạo Order
        Order order = Order.builder()
                .user(cart.getUser())
                .fullName(fullName)
                .phoneNumber(phone)
                .address(address)
                .totalMoney(totalMoney)
                .status(OrderStatus.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);

        // 3. Tạo OrderDetail
        for (CartItem item : cartItems) {
            ProductVariant variant = item.getProductVariant();
            Product product = variant.getProduct();

            OrderDetail detail = OrderDetail.builder()
                    .order(savedOrder)
                    .productId(product.getId()) // Lưu ID để tham chiếu lỏng
                    .productName(product.getName())
                    .size(variant.getSize().getName()) // Giả sử Size có getName()
                    .price(product.getPrice())
                    .quantity(item.getQuantity())
                    .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .build();

            orderDetailRepository.save(detail);
        }
        cartService.clearCart(cart);
        return savedOrder;
    }

    public Order getOrderByIdAndUser(Long orderId, String username){
        Order order = orderRepository.findByIdWithDetails(orderId).orElseThrow(() -> new RuntimeException("Order not found"));

        if(!order.getUser().getUsername().equals(username)){
            throw new RuntimeException("khong có quyền truy cập");
        }
        return order;
    }

    @Transactional
    public void updateOrderInfo(Long orderId, String phoneNumber, String address, String username) {
        Order order = getOrderByIdAndUser(orderId, username);

        // Chỉ cho phép sửa khi trạng thái là PENDING
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Đơn hàng đã được duyệt hoặc đang giao, không thể thay đổi thông tin!");
        }

        order.setPhoneNumber(phoneNumber);
        order.setAddress(address);
        orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long orderId, String username) {
        Order order = getOrderByIdAndUser(orderId, username);

        // Chỉ cho phép hủy khi trạng thái là PENDING
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Đơn hàng đã được duyệt, không thể hủy!");
        }

        order.setStatus(OrderStatus.CANCELLED); // Chuyển trạng thái thành Đã hủy
        order.setCancelledAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    // ============================================================
    // Task 0.5: State machine + new methods (confirmReceived, refundOrder, adminUpdateStatus)
    // ============================================================

    /**
     * User bấm "Đã nhận hàng" → chuyển DELIVERED → COMPLETED.
     * Hook cộng điểm loyalty sẽ được thêm ở Batch 1 (PointService).
     * Ở Batch 0 chỉ set status + completedAt.
     */
    @Transactional
    public Order confirmReceived(Long orderId, String username) {
        Order order = getOrderByIdAndUser(orderId, username);

        if (!order.getStatus().canTransitionTo(OrderStatus.COMPLETED)) {
            throw new IllegalOrderTransitionException(
                    "Không thể xác nhận nhận hàng từ trạng thái " + order.getStatus()
                            + ". Chỉ chấp nhận khi đơn đang DELIVERED.");
        }

        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    /**
     * Admin hoặc hệ thống hoàn tiền → CONFIRMED/SHIPPING/DELIVERED → REFUNDED.
     * Hook trừ điểm loyalty sẽ được thêm ở Batch 1.
     */
    @Transactional
    public Order refundOrder(Long orderId, String adminUsername) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (!order.getStatus().canTransitionTo(OrderStatus.REFUNDED)) {
            throw new IllegalOrderTransitionException(
                    "Không thể hoàn tiền từ trạng thái " + order.getStatus()
                            + ". Chỉ chấp nhận khi đơn đã CONFIRMED/SHIPPING/DELIVERED.");
        }

        order.setStatus(OrderStatus.REFUNDED);
        order.setRefundedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    /**
     * Admin cập nhật trạng thái qua state machine.
     * Áp dụng cho OrderManagementController + cron auto-transition.
     */
    @Transactional
    public Order adminUpdateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        OrderStatus current = order.getStatus();
        if (!current.canTransitionTo(newStatus)) {
            throw new IllegalOrderTransitionException(
                    "Transition không hợp lệ: " + current + " → " + newStatus);
        }

        order.setStatus(newStatus);
        // Set timestamp tương ứng
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
        return orderRepository.save(order);
    }
}
