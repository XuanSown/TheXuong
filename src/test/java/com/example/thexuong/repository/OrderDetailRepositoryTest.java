package com.example.thexuong.repository;

import com.example.thexuong.entity.Order;
import com.example.thexuong.entity.OrderDetail;
import com.example.thexuong.entity.OrderStatus;
import com.example.thexuong.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
public class OrderDetailRepositoryTest {

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void shouldSaveOrderDetailWithSize() {
        // Create a dummy user and order to satisfy foreign key constraints
        User user = new User();
        user.setUsername("testuser" + System.currentTimeMillis());
        user.setEmail("test" + System.currentTimeMillis() + "@example.com");
        user.setPassword("password");
        user = userRepository.save(user);

        Order order = Order.builder()
                .user(user)
                .fullName("Test User")
                .phoneNumber("0123456789")
                .address("Test Address")
                .totalMoney(new BigDecimal("100.00"))
                .status(OrderStatus.PENDING)
                .build();
        order = orderRepository.save(order);

        OrderDetail detail = OrderDetail.builder()
                .order(order)
                .productId(1L)
                .productName("Test Product")
                .size("XL") // This is the column that is missing
                .price(new BigDecimal("100.00"))
                .quantity(1)
                .totalPrice(new BigDecimal("100.00"))
                .build();

        OrderDetail savedDetail = orderDetailRepository.save(detail);
        assertNotNull(savedDetail.getId());
    }
}
