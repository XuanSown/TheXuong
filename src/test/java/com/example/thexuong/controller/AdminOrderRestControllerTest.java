package com.example.thexuong.controller;

import com.example.thexuong.entity.Order;
import com.example.thexuong.entity.OrderStatus;
import com.example.thexuong.entity.User;
import com.example.thexuong.repository.OrderRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AdminOrderRestControllerTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long testOrderId;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setEmail("adminorder@gmail.com");
        user.setFullName("Admin Order");
        user.setPassword("pass");
        user.setRole("ADMIN");
        userRepository.save(user);

        Order order = new Order();
        order.setUser(user);
        order.setFullName("Nguyễn Khách");
        order.setPhoneNumber("0987654321");
        order.setStatus(OrderStatus.PENDING);
        order.setTotalMoney(new BigDecimal("500000"));
        orderRepository.save(order);

        testOrderId = order.getId();
    }

    @Test
    @WithMockUser(username = "adminorder@gmail.com", authorities = {"ADMIN"})
    void testGetOrders() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/admin/orders")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].fullName", is("Nguyễn Khách")));
    }

    @Test
    @WithMockUser(username = "adminorder@gmail.com", authorities = {"ADMIN"})
    void testUpdateOrderStatus() throws Exception {
        Map<String, String> req = Map.of("status", "CONFIRMED");

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/admin/orders/" + testOrderId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Cập nhật trạng thái thành công")))
                .andExpect(jsonPath("$.order.status", is("CONFIRMED")));
    }

    @Test
    @WithMockUser(username = "user@gmail.com", authorities = {"USER"})
    void testGetOrders_Forbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/admin/orders"))
                .andExpect(status().isForbidden());
    }
}
