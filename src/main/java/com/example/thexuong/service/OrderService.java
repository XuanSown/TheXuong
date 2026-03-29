package com.example.thexuong.service;

import com.example.thexuong.entity.*;
import com.example.thexuong.repository.OrderDetailRepository;
import com.example.thexuong.repository.OrderRepository;
import com.example.thexuong.repository.ProductVariantRepository; // THÊM IMPORT NÀY
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

    // THÊM REPOSITORY NÀY ĐỂ XỬ LÝ SỐ LƯỢNG
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

        // 3. Tạo OrderDetail VÀ TRỪ SỐ LƯỢNG TỒN KHO
        for (CartItem item : cartItems) {
            ProductVariant variant = item.getProductVariant();
            Product product = variant.getProduct();

            // KIỂM TRA TỒN KHO
            if (variant.getQuantity() < item.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + product.getName() + " (Size " + variant.getSize().getName() + ") không đủ số lượng trong kho!");
            }

            // TRỪ SỐ LƯỢNG TRONG KHO VÀ LƯU LẠI
            variant.setQuantity(variant.getQuantity() - item.getQuantity());
            productVariantRepository.save(variant);

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

        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("Đơn hàng đã được duyệt, không thể hủy!");
        }

        order.setStatus("CANCELLED");
        orderRepository.save(order);

        // Lưu ý: Nếu hủy đơn, bạn có thể cân nhắc code thêm đoạn cộng lại số lượng vào kho ở đây
    }
}