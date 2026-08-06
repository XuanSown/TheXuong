package com.example.thexuong.controller;

import com.example.thexuong.entity.Cart;
import com.example.thexuong.entity.Product;
import com.example.thexuong.entity.ProductVariant;
import com.example.thexuong.entity.User;
import com.example.thexuong.repository.CartRepository;
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
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;

public class CartRestControllerTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long testVariantId;

    @BeforeEach
    void setUp() {
        cartRepository.deleteAll();
        productVariantRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        // Tao User
        User user = new User();
        user.setEmail("testcart@gmail.com");
        user.setFullName("Test Cart");
        user.setPassword("pass");
        user.setRole("USER");
        userRepository.save(user);

        // Tao Product & Variant
        Product p = new Product();
        p.setName("Giày Cart");
        p.setPrice(new BigDecimal("200000"));
        p.setActive(true);
        productRepository.save(p);

        ProductVariant variant = new ProductVariant();
        variant.setProduct(p);
        variant.setQuantity(100);
        productVariantRepository.save(variant);

        testVariantId = variant.getId();
    }

    @Test
    @WithMockUser(username = "testcart@gmail.com")
    void testGetCart_Empty() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.total").value(0.0));
    }

    @Test
    @WithMockUser(username = "testcart@gmail.com")
    void testAddToCart() throws Exception {
        Map<String, Object> req = Map.of(
                "variantId", testVariantId,
                "quantity", 2
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.total").value(400000.0));
    }

    @Test
    @WithMockUser(username = "testcart@gmail.com")
    void testClearCart() throws Exception {
        // Add to cart first
        Map<String, Object> req = Map.of("variantId", testVariantId, "quantity", 2);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/cart/clear"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));
    }
}
