package com.example.thexuong.service;

import com.example.thexuong.entity.*;
import com.example.thexuong.exception.IllegalOrderTransitionException;
import com.example.thexuong.repository.*;
import com.example.thexuong.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
private final OrderRepository orderRepository;
private final CartService cartService;
private final OrderDetailRepository orderDetailRepository;
private final PointService pointService;
private final VoucherService voucherService;
private final UserRepository userRepository;
private final PointTierService pointTierService;
private final PointTierRepository pointTierRepository;
private final OrderEventService orderEventService;
private final EmailService emailService;
private final UserPointsRepository userPointsRepository;
private final InventoryService inventoryService;
private final ProductVariantRepository productVariantRepository;

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

// Tính discount tự động từ hạng trước khi trừ điểm
BigDecimal tierDiscountAcc = BigDecimal.ZERO;
if (userId != null) {
    User u = userRepository.findById(userId).orElse(null);
    if (u != null && u.getTierCode() != null) {
        PointTier tier = pointTierRepository.findByCode(u.getTierCode()).orElse(null);
        if (tier != null && tier.getAutoDiscountPercent() != null && tier.getAutoDiscountPercent().compareTo(BigDecimal.ZERO) > 0) {
            tierDiscountAcc = subtotal.multiply(tier.getAutoDiscountPercent())
                    .divide(new BigDecimal("100"), 0, java.math.RoundingMode.HALF_UP);
        }
    }
}
discountAmount = discountAmount.add(tierDiscountAcc);

// Trừ điểm thưởng (cap lại tối đa bằng số tiền còn lại phải trả)
if (pointsToUse != null && pointsToUse > 0 && userId != null) {
    int currentPoints = pointService.getCurrentPoints(userId);
    if (currentPoints < pointsToUse) {
        throw new RuntimeException("Bạn chỉ có " + currentPoints + " điểm, không đủ " + pointsToUse + " điểm.");
    }
    
    BigDecimal remainingToPay = subtotal.add(shippingFee).subtract(discountAmount);
    int maxPointsUsable = remainingToPay.compareTo(BigDecimal.ZERO) > 0 ? remainingToPay.intValue() : 0;
    int actualPointsToDeduct = Math.min(pointsToUse, maxPointsUsable);

    if (actualPointsToDeduct > 0) {
        pointService.spendPoints(userId, actualPointsToDeduct,
            "Đổi " + actualPointsToDeduct + " điểm tại đơn (giảm " + actualPointsToDeduct + "đ)");
        actualPointsUsed = actualPointsToDeduct;
        discountAmount = discountAmount.add(BigDecimal.valueOf(actualPointsUsed));
    }
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
java.util.List<OrderDetail> detailsList = new java.util.ArrayList<>();

for (CartItem item : cartItems) {
ProductVariant variant = item.getProductVariant();
Product product = variant.getProduct();

OrderDetail detail = OrderDetail.builder()
.order(savedOrder)
.productId(product.getId())
.productVariantId(variant.getId())
.productName(product.getName())
.size(variant.getSize().getName())
.price(product.getPrice())
.quantity(item.getQuantity())
.totalPrice(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
.build();

orderDetailRepository.save(detail);
detailsList.add(detail);

// Phase 3: Trừ stock sau khi tạo OrderDetail
// Nếu deduct fail → rollback toàn bộ order (đã có @Transactional)
inventoryService.deductStock(
product.getId(),
variant.getSize().getId(),
item.getQuantity()
);
}
savedOrder.setOrderDetails(detailsList);
cartService.clearCart(cart);

if (savedOrder.getUser() != null) {
pointTierService.setFirstOrderTier(savedOrder.getUser().getId());
}

orderEventService.recordTransition(savedOrder.getId(), null, "PENDING",
savedOrder.getUser() != null ? savedOrder.getUser().getId() : null, "CUSTOMER",
"Đơn hàng mới được tạo");

return savedOrder;
}

/**
* Kiểm tra quyền sở hữu đơn hàng bằng userId (an toàn hơn username).
* Ném RuntimeException nếu user không phải chủ đơn hàng.
*/
public Order getOrderByIdAndUser(Long orderId, Long userId){
Order order = orderRepository.findByIdWithDetails(orderId)
.orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại: " + orderId));

if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
throw new RuntimeException("Bạn không có quyền truy cập đơn hàng này");
}
return order;
}

@Transactional
public void updateOrderInfo(Long orderId, String phoneNumber, String address, Long userId) {
Order order = getOrderByIdAndUser(orderId, userId);

if (order.getStatus() != OrderStatus.PENDING) {
throw new RuntimeException("Đơn hàng đã được duyệt hoặc đang giao, không thể thay đổi thông tin!");
}

order.setPhoneNumber(phoneNumber);
order.setAddress(address);
orderRepository.save(order);
}

private void restoreStockForOrder(Order order) {
        for (OrderDetail detail : order.getOrderDetails()) {
            try {
                Long sizeId = null;
                if (detail.getProductVariantId() != null) {
                    sizeId = productVariantRepository.findById(detail.getProductVariantId())
                            .map(pv -> pv.getSize().getId())
                            .orElse(null);
                }
                if (sizeId == null) {
                    sizeId = productVariantRepository
                            .findByProductIdAndSizeName(detail.getProductId(), detail.getSize())
                            .map(pv -> pv.getSize().getId())
                            .orElse(null);
                }

                if (sizeId != null) {
                    inventoryService.restoreStock(detail.getProductId(), sizeId, detail.getQuantity());
                    log.info("Restored stock for order #{}: productId={}, size={}, qty={}",
                            order.getId(), detail.getProductId(), detail.getSize(), detail.getQuantity());
                } else {
                    log.warn("Cannot find variant for order #{}: productId={}, size={} — stock not restored",
                            order.getId(), detail.getProductId(), detail.getSize());
                }
            } catch (Exception e) {
                log.error("Failed to restore stock for order #{}: productId={}, size={}",
                        order.getId(), detail.getProductId(), detail.getSize(), e);
            }
        }
    }

    private void restoreLoyaltyForOrder(Order order) {
        if (order.getUser() != null && order.getPointsUsed() != null && order.getPointsUsed() > 0) {
            try {
                pointService.refundSpentPoints(order.getUser().getId(), order.getPointsUsed(), order.getId());
            } catch (Exception e) {
                log.error("Failed to refund points for order #{}: {}", order.getId(), e.getMessage(), e);
            }
        }
        if (order.getVoucherCode() != null && !order.getVoucherCode().isBlank()) {
            try {
                voucherService.restoreVoucher(order.getVoucherCode());
            } catch (Exception e) {
                log.error("Failed to restore voucher for order #{}: {}", order.getId(), e.getMessage(), e);
            }
        }
    }

@Transactional
public void cancelOrder(Long orderId, Long userId) {
Order order = getOrderByIdAndUser(orderId, userId);

if (order.getStatus() != OrderStatus.PENDING) {
throw new RuntimeException("Đơn hàng đã được duyệt, không thể yêu cầu hủy!");
}

restoreStockForOrder(order);
restoreLoyaltyForOrder(order);

order.setStatus(OrderStatus.CANCEL_REQUESTED);
orderRepository.save(order);
}

@Transactional
public Order confirmReceived(Long orderId, Long userId) {
Order order = getOrderByIdAndUser(orderId, userId);

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
log.info("[LOYALTY] User {} earned {} points from order #{}",
saved.getUser().getId(), points, saved.getId());
}
}
} catch (Exception e) {
log.error("[LOYALTY ERROR] Failed to earn points for order #{}: {}", saved.getId(), e.getMessage(), e);
}

try {
boolean upgraded = pointTierService.upgradeTierIfEligible(saved.getUser().getId());
if (upgraded) {
log.info("[TIER] User {} upgraded to {}", saved.getUser().getId(), saved.getUser().getTierCode());
if (saved.getUser() != null && saved.getUser().getEmail() != null) {
emailService.sendVipWelcome(saved.getUser().getEmail(), saved.getUser().getFullName());
}
}
} catch (Exception e) {
log.error("[TIER ERROR] Failed to upgrade tier: {}", e.getMessage(), e);
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
log.error("[EMAIL ERROR] Failed to send points earned email: {}", e.getMessage(), e);
}

orderEventService.recordTransition(saved.getId(), "DELIVERED", "COMPLETED",
saved.getUser() != null ? saved.getUser().getId() : null, "CUSTOMER",
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

restoreStockForOrder(order);
restoreLoyaltyForOrder(order);

order.setStatus(OrderStatus.REFUNDED);
order.setRefundedAt(LocalDateTime.now());
Order saved = orderRepository.save(order);

try {
pointService.reversePoints(saved.getId(),
"Hoàn điểm từ refund đơn #" + saved.getId());
} catch (Exception e) {
log.error("[LOYALTY ERROR] Failed to reverse points for order #{}: {}", saved.getId(), e.getMessage(), e);
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

if (newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.REFUNDED) {
    restoreStockForOrder(order);
    restoreLoyaltyForOrder(order);
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
