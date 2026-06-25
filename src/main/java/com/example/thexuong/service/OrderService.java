package com.example.thexuong.service;

import com.example.thexuong.entity.*;
import com.example.thexuong.exception.IllegalOrderTransitionException;
import com.example.thexuong.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final OrderDetailRepository orderDetailRepository;
    private final PointService pointService;
    private final VoucherService voucherService;
    private final UserRepository userRepository;
    private final PointTierService pointTierService;
    private final OrderEventService orderEventService;
    private final EmailService emailService;
    private final UserPointsRepository userPointsRepository;

    @Transactional
    public Order placeOrder(String username, String fullName, String phone, String address,
                            String voucherCode, Integer pointsToUse) {
        Cart cart = cartService.getCartByUser(username);
        List<CartItem> cartItems = cart.getItems();

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống");
        }

        BigDecimal subtotal = cartItems.stream()
                .map(item -> item.getProductVariant().getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingFee = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        String appliedVoucherCode = null;
        int actualPointsUsed = 0;

        Long userId = null;
        if (voucherCode != null && !voucherCode.isBlank() || (pointsToUse != null && pointsToUse > 0)) {
            User user = userRepository.findByEmail(username)
                    .orElseGet(() -> userRepository.findByUsername(username).orElse(null));
            if (user == null) {
                throw new RuntimeException("Không tìm thấy user: " + username);
            }
            userId = user.getId();
        }

        if (voucherCode != null && !voucherCode.isBlank() && userId != null) {
            discountAmount = voucherService.validateAndGetDiscount(voucherCode, userId, subtotal);
            appliedVoucherCode = voucherCode;
        }

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

        BigDecimal totalMoney = subtotal.add(shippingFee).subtract(discountAmount);
        if (totalMoney.compareTo(BigDecimal.ZERO) < 0) {
            totalMoney = BigDecimal.ZERO;
        }

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

        for (CartItem item : cartItems) {
            ProductVariant variant = item.getProductVariant();
            Product product = variant.getProduct();

            OrderDetail detail = OrderDetail.builder()
                    .order(savedOrder)
                    .productId(product.getId())
                    .productName(product.getName())
                    .size(variant.getSize().getName())
                    .price(product.getPrice())
                    .quantity(item.getQuantity())
                    .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .build();

            orderDetailRepository.save(detail);
        }
        cartService.clearCart(cart);

        if (savedOrder.getUser() != null) {
            pointTierService.setFirstOrderTier(savedOrder.getUser().getId());
        }

        orderEventService.recordTransition(savedOrder.getId(), null, "PENDING",
                savedOrder.getUser() != null ? savedOrder.getUser().getId() : null, "USER",
                "Đơn hàng mới được tạo");

        return savedOrder;
    }

    public Order getOrderByIdAndUser(Long orderId, String username){
        Order order = orderRepository.findByIdWithDetails(orderId).orElseThrow(() -> new RuntimeException("Order not found"));

        if(!order.getUser().getUsername().equals(username)){
            throw new RuntimeException("không có quyền truy cập");
        }
        return order;
    }

    @Transactional
    public void updateOrderInfo(Long orderId, String phoneNumber, String address, String username) {
        Order order = getOrderByIdAndUser(orderId, username);

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

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Đơn hàng đã được duyệt, không thể hủy!");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        orderRepository.save(order);
    }

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

        int points = 0;
        try {
            if (saved.getTotalForPointCalc() != null && saved.getUser() != null) {
                points = pointService.earnPoints(
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

        try {
            boolean upgraded = pointTierService.upgradeTierIfEligible(saved.getUser().getId());
            if (upgraded) {
                System.out.println("[TIER] User " + saved.getUser().getId()
                        + " upgraded to " + saved.getUser().getTierCode());
                if (saved.getUser() != null && saved.getUser().getEmail() != null) {
                    emailService.sendVipWelcome(saved.getUser().getEmail(), saved.getUser().getFullName());
                }
            }
        } catch (Exception e) {
            System.err.println("[TIER ERROR] Failed to upgrade tier: " + e.getMessage());
        }

        try {
            if (points > 0 && saved.getUser() != null && saved.getUser().getEmail() != null) {
                int currentBalance = userPointsRepository.findByUserId(saved.getUser().getId())
                        .map(UserPoints::getCurrentPoints).orElse(0);
                emailService.sendPointsEarned(
                        saved.getUser().getEmail(),
                        saved.getUser().getFullName() != null ? saved.getUser().getFullName() : saved.getUser().getUsername(),
                        points,
                        saved.getId(),
                        currentBalance
                );
            }
        } catch (Exception e) {
            System.err.println("[EMAIL ERROR] Failed to send points earned email: " + e.getMessage());
        }

        orderEventService.recordTransition(saved.getId(), "DELIVERED", "COMPLETED",
                saved.getUser() != null ? saved.getUser().getId() : null, "USER",
                "Khách xác nhận đã nhận hàng");

        return saved;
    }

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

        try {
            pointService.reversePoints(saved.getId(),
                    "Hoàn điểm từ refund đơn #" + saved.getId());
        } catch (Exception e) {
            System.err.println("[LOYALTY ERROR] Failed to reverse points for order #"
                    + saved.getId() + ": " + e.getMessage());
        }

        return saved;
    }

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
