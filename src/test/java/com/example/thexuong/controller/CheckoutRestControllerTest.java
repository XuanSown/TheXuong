package com.example.thexuong.controller;

import com.example.thexuong.entity.User;
import com.example.thexuong.repository.UserRepository;
import com.example.thexuong.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CheckoutRestControllerTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User user = new User();
        user.setEmail("checkout@gmail.com");
        user.setFullName("Checkout User");
        user.setPassword("pass");
        user.setRole("USER");
        userRepository.save(user);

        // Ensure user has a cart
        cartService.getCartByUser(user.getEmail());
    }

    @Test
    @WithMockUser(username = "checkout@gmail.com")
    void testGetCheckoutData() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/checkout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email", is("checkout@gmail.com")))
                .andExpect(jsonPath("$.cart.total").value(0.0));
    }

    @Test
    @WithMockUser(username = "checkout@gmail.com")
    void testGetPoints() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/checkout/points"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPoints", is(0)));
    }
}
