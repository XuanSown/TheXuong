package com.example.thexuong.controller;

import com.example.thexuong.dto.order.PlaceOrderRequest;
import com.example.thexuong.entity.Cart;
import com.example.thexuong.entity.CartItem;
import com.example.thexuong.entity.Order;
import com.example.thexuong.entity.Product;
import com.example.thexuong.entity.ProductVariant;
import com.example.thexuong.entity.User;
import com.example.thexuong.repository.CartRepository;
import com.example.thexuong.repository.OrderRepository;
import com.example.thexuong.repository.ProductRepository;
import com.example.thexuong.repository.ProductVariantRepository;
import com.example.thexuong.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

public class OrderRestControllerTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.example.thexuong.repository.SizeRepository sizeRepository;

    @Autowired
    private com.example.thexuong.service.CartService cartService;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        productVariantRepository.deleteAll();
        sizeRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setEmail("testorder@gmail.com");
        user.setFullName("Test Order");
        user.setPassword("pass");
        user.setRole("USER");
        userRepository.save(user);

        com.example.thexuong.entity.Size s = new com.example.thexuong.entity.Size();
        s.setName("XL");
        sizeRepository.save(s);

        Product p = new Product();
        p.setName("Sản phẩm Order");
        p.setPrice(new BigDecimal("150000"));
        p.setActive(true);
        productRepository.save(p);

        ProductVariant variant = new ProductVariant();
        variant.setProduct(p);
        variant.setSize(s);
        variant.setQuantity(50);
        productVariantRepository.save(variant);

        cartService.addToCart(user.getEmail(), variant.getId(), 2);
    }

    @Test
    @WithMockUser(username = "testorder@gmail.com")
    void testPlaceOrder_COD() throws Exception {
        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setFullName("Nguyễn Khách");
        req.setPhoneNumber("0999999999");
        req.setAddress("Hà Nội");
        req.setPaymentMethod("COD");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Đặt hàng thành công")))
                .andExpect(jsonPath("$.order.totalMoney").value(300000.0)); // 2 * 150000
    }

    @Test
    @WithMockUser(username = "testorder@gmail.com")
    void testGetMyOrders() throws Exception {
        // First place an order manually or via API
        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setFullName("Nguyễn Khách");
        req.setPhoneNumber("0999999999");
        req.setAddress("Hà Nội");
        req.setPaymentMethod("COD");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fullName", is("Nguyễn Khách")));
    }
}
