package com.example.thexuong.service;

import com.example.thexuong.entity.*;
import com.example.thexuong.exception.IllegalOrderTransitionException;
import com.example.thexuong.repository.OrderDetailRepository;
import com.example.thexuong.repository.OrderRepository;
import com.example.thexuong.repository.UserRepository;
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
    @Autowired
    private final PointService pointService;  // Task 1.16-1.17: hook loyalty
    @Autowired
    private final VoucherService voucherService;  // Task 3.5: áp voucher trong placeOrder
    @Autowired
    private final UserRepository userRepository;  // Task 3.5: resolve user từ username
    @Autowired
    private final PointTierService pointTierService;  // Batch 4: hook tier upgrade
    @Autowired
    private final OrderEventService orderEventService;  // Batch 4: log status transitions

    @Transactional
    public Order placeOrder(String username, String fullName, String phone, String address,
                            String voucherCode, Integer pointsToUse) {
        Cart cart = cartService.getCartByUser(username);
        List<CartItem> cartItems = cart.getItems();

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống");
        }

        // 1. Tính subtotal (tổng tiền hàng, KHÔNG bao gồm ship/voucher)
        BigDecimal subtotal = cartItems.stream()
                .map(item -> item.getProductVariant().getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingFee = BigDecimal.ZERO;  // TODO Batch 4: tính ship (VIP free)
        BigDecimal discountAmount = BigDecimal.ZERO;
        String appliedVoucherCode = null;
        int actualPointsUsed = 0;

        // 2. Resolve userId (1 lần, dùng cho cả voucher và points)
        Long userId = null;
        if (voucherCode != null && !voucherCode.isBlank() || (pointsToUse != null && pointsToUse > 0)) {
            User user = userRepository.findByEmail(username)
                    .orElseGet(() -> userRepository.findByUsername(username).orElse(null));
            if (user == null) {
                throw new RuntimeException("Không tìm thấy user: " + username);
            }
            userId = user.getId();
        }

        // 3. Áp voucher nếu có
        if (voucherCode != null && !voucherCode.isBlank() && userId != null) {
            discountAmount = voucherService.validateAndGetDiscount(voucherCode, userId, subtotal);
            appliedVoucherCode = voucherCode;
        }

        // 4. Áp điểm nếu có (1 điểm = 1đ giảm)
        if (pointsToUse != null && pointsToUse > 0 && userId != null) {
            int currentPoints = pointService.getCurrentPoints(userId);
            if (currentPoints < pointsToUse) {
                throw new RuntimeException("Bạn chỉ có " + currentPoints + " điểm, không đủ " + pointsToUse + " điểm.");
            }
            pointService.spendPoints(userId, pointsToUse,
                    "Đổi " + pointsToUse + " điểm tại đơn (giảm " + pointsToUse + "đ)");
            actualPointsUsed = pointsToUse;
            discountAmount = discountAmount.add(BigDecimal.valueOf(actualPointsUsed));
        }

        // 5. Tính totalMoney (subtotal + ship - discount)
        BigDecimal totalMoney = subtotal.add(shippingFee).subtract(discountAmount);
        if (totalMoney.compareTo(BigDecimal.ZERO) < 0) {
            totalMoney = BigDecimal.ZERO;
        }

        // 6. Tạo Order (snapshot total_for_point_calc = subtotal — theo rule đã chốt ở voucher.md mục 1)
        Order order = Order.builder()
                .user(cart.getUser())
                .fullName(fullName)
                .phoneNumber(phone)
                .address(address)
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .discountAmount(discountAmount)
                .pointsUsed(actualPointsUsed)
                .voucherCode(appliedVoucherCode)
                .totalForPointCalc(subtotal)
                .totalMoney(totalMoney)
                .status(OrderStatus.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);

        // 7. Tạo OrderDetail
        for (CartItem item : cartItems) {
            ProductVariant variant = item.getProductVariant();
            Product product = variant.getProduct();

            OrderDetail detail = OrderDetail.builder()
                    .order(savedOrder)
                    .productId(product.getId()) // Lưu ID để tham chiếu lỏng
                    .productName(product.getName())
                    .size(variant.getSize().getName())
                    .price(product.getPrice())
                    .quantity(item.getQuantity())
                    .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .build();

            orderDetailRepository.save(detail);
        }
        cartService.clearCart(cart);

        // Task 4.5: Set tier THUONG cho user lần đầu (nếu chưa có tier)
        if (savedOrder.getUser() != null) {
            pointTierService.setFirstOrderTier(savedOrder.getUser().getId());
        }

        // Task 4.9: Log event PENDING
        orderEventService.recordTransition(savedOrder.getId(), null, "PENDING",
                savedOrder.getUser() != null ? savedOrder.getUser().getId() : null, "USER",
                "Đơn hàng mới được tạo");

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

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    // ============================================================
    // Task 0.5: State machine + new methods (confirmReceived, refundOrder, adminUpdateStatus)
    // ============================================================

    /**
     * User bấm "Đã nhận hàng" → chuyển DELIVERED → COMPLETED.
     * Task 1.16: Hook cộng điểm loyalty (PointService.earnPoints) dựa trên totalForPointCalc (= subtotal).
     * Nếu lỗi khi cộng điểm → KHÔNG block flow chính (chỉ log warn, đơn vẫn COMPLETED).
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
        Order saved = orderRepository.save(order);

        // Hook loyalty: cộng điểm dựa trên totalForPointCalc (snapshot, không bao gồm voucher discount)
        try {
            if (saved.getTotalForPointCalc() != null && saved.getUser() != null) {
                int points = pointService.earnPoints(
                        saved.getUser().getId(),
                        saved.getId(),
                        saved.getTotalForPointCalc(),
                        "Cộng điểm từ đơn #" + saved.getId());
                if (points > 0) {
                    System.out.println("[LOYALTY] User " + saved.getUser().getId()
                            + " earned " + points + " points from order #" + saved.getId());
                }
            }
        } catch (Exception e) {
            System.err.println("[LOYALTY ERROR] Failed to earn points for order #"
                    + saved.getId() + ": " + e.getMessage());
        }

        // Task 4.5: Check nâng tier sau khi earn points
        try {
            boolean upgraded = pointTierService.upgradeTierIfEligible(saved.getUser().getId());
            if (upgraded) {
                System.out.println("[TIER] User " + saved.getUser().getId()
                        + " upgraded to " + saved.getUser().getTierCode());
                // TODO: gửi email sendVipWelcome nếu lần đầu lên VIP
            }
        } catch (Exception e) {
            System.err.println("[TIER ERROR] Failed to upgrade tier: " + e.getMessage());
        }

        // Task 4.9: Log event COMPLETED
        orderEventService.recordTransition(saved.getId(), "DELIVERED", "COMPLETED",
                saved.getUser().getId(), "USER", "Khách xác nhận đã nhận hàng");

        return saved;
    }
    /**
     * Admin hoặc hệ thống hoàn tiền → CONFIRMED/SHIPPING/DELIVERED → REFUNDED.
     * Task 1.17: Hook trừ điểm loyalty (PointService.reversePoints).
     * Task 3.12 (hoàn tiền → cũng nên hoàn voucher nếu có): nếu order có voucher USED → set UserVoucher status về ACTIVE lại.
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
        Order saved = orderRepository.save(order);

        // Hook loyalty: trừ điểm đã cộng (nếu có)
        try {
            pointService.reversePoints(saved.getId(),
                    "Hoàn điểm từ refund đơn #" + saved.getId());
        } catch (Exception e) {
            System.err.println("[LOYALTY ERROR] Failed to reverse points for order #"
                    + saved.getId() + ": " + e.getMessage());
        }

        return saved;
    }

    /**
     * Admin cập nhật trạng thái qua state machine.
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
