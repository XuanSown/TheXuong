package com.example.thexuong.service;

import com.example.thexuong.entity.*;
import com.example.thexuong.repository.OrderDetailRepository;
import com.example.thexuong.repository.OrderRepository;
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

    @Transactional
    public void placeOrder(String username, String fullName, String phone, String address) {
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
}
