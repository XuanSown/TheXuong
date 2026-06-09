package com.example.thexuong.service;

import com.example.thexuong.entity.*;
import com.example.thexuong.repository.OrderDetailRepository;
import com.example.thexuong.repository.OrderRepository;
import com.example.thexuong.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final ProductVariantRepository productVariantRepository;

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
                .status("PENDING")
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

    public Order getOrderByIdAndUser(Long orderId, String identifier){
        Order order = orderRepository.findByIdWithDetails(orderId).orElseThrow(() -> new RuntimeException("Order not found"));

        if(!order.getUser().getUsername().equals(identifier) && !identifier.equals(order.getUser().getEmail())){
            throw new RuntimeException("khong có quyền truy cập");
        }
        return order;
    }

    @Transactional
    public void updateOrderInfo(Long orderId, String phoneNumber, String address, String username) {
        Order order = getOrderByIdAndUser(orderId, username);

        // Chỉ cho phép sửa khi trạng thái là PENDING
        if (!"PENDING".equals(order.getStatus())) {
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
        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("Đơn hàng đã được duyệt, không thể hủy!");
        }

        order.setStatus("CANCELLED"); // Chuyển trạng thái thành Đã hủy
        orderRepository.save(order);

    }

    @Transactional
    public void adminUpdateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        String oldStatus = order.getStatus();

        // 1. Chuyển sang ĐÃ DUYỆT (APPROVED)
        // -> Trừ số lượng tồn kho
        if (!"APPROVED".equals(oldStatus) && "APPROVED".equals(newStatus)) {
            for (OrderDetail detail : order.getOrderDetails()) {
                ProductVariant variant = productVariantRepository.findByProductIdAndSizeName(detail.getProductId(), detail.getSize()).orElse(null);
                if (variant != null) {
                    int newQuantity = variant.getQuantity() - detail.getQuantity();
                    if (newQuantity < 0) {
                        throw new RuntimeException("Sản phẩm " + detail.getProductName() + " (Size: " + detail.getSize() + ") không đủ số lượng tồn kho!");
                    }
                    variant.setQuantity(newQuantity);
                    productVariantRepository.save(variant);
                }
            }
        }

        // 2. Chuyển sang ĐÃ HỦY (CANCELLED)
        // -> Cộng lại số lượng tồn kho nếu trước đó đã trừ (tức là đã duyệt, hoặc đã giao)
        if (("APPROVED".equals(oldStatus) || "SHIPPED".equals(oldStatus)) && "CANCELLED".equals(newStatus)) {
            for (OrderDetail detail : order.getOrderDetails()) {
                ProductVariant variant = productVariantRepository.findByProductIdAndSizeName(detail.getProductId(), detail.getSize()).orElse(null);
                if (variant != null) {
                    variant.setQuantity(variant.getQuantity() + detail.getQuantity());
                    productVariantRepository.save(variant);
                }
            }
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
    }
}
