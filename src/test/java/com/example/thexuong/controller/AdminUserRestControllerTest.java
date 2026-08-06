package com.example.thexuong.controller;

import com.example.thexuong.entity.User;
import com.example.thexuong.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AdminUserRestControllerTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.example.thexuong.repository.OrderRepository orderRepository;

    @Autowired
    private com.example.thexuong.repository.CartRepository cartRepository;

    @Autowired
    private com.example.thexuong.repository.UserAddressRepository userAddressRepository;

    @Autowired
    private com.example.thexuong.repository.UserVoucherRepository userVoucherRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long targetUserId;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        userAddressRepository.deleteAll();
        userVoucherRepository.deleteAll();
        userRepository.deleteAll();

        // Admin User (the one performing actions)
        User admin = new User();
        admin.setEmail("adminuser@gmail.com");
        admin.setFullName("Admin");
        admin.setPassword("pass");
        admin.setRole("ADMIN");
        admin.setActive(true);
        userRepository.save(admin);

        // Target User
        User target = new User();
        target.setEmail("target@gmail.com");
        target.setFullName("Target User");
        target.setPassword("pass");
        target.setRole("CUSTOMER");
        target.setActive(true);
        userRepository.save(target);

        targetUserId = target.getId();
    }

    @Test
    @WithMockUser(username = "adminuser@gmail.com", authorities = {"ADMIN"})
    void testGetAllUsers() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2))); // admin + target
    }

    @Test
    @WithMockUser(username = "adminuser@gmail.com", authorities = {"ADMIN"})
    void testToggleActive() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/admin/users/" + targetUserId + "/toggle-active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active", is(false)));
    }

    @Test
    @WithMockUser(username = "adminuser@gmail.com", authorities = {"ADMIN"})
    void testCreateUser() throws Exception {
        Map<String, String> req = Map.of(
                "email", "newuser@gmail.com",
                "fullName", "New User",
                "password", "123456",
                "role", "CUSTOMER"
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email", is("newuser@gmail.com")));
    }
}
