package com.example.thexuong.controller;

import com.example.thexuong.dto.auth.LoginRequest;
import com.example.thexuong.dto.auth.RegisterRequest;
import com.example.thexuong.dto.auth.UpdateProfileRequest;
import com.example.thexuong.entity.User;
import com.example.thexuong.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import jakarta.servlet.http.Cookie;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthRestControllerTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testRegister_Success() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Nguyễn Văn Test");
        req.setEmail("testregister@gmail.com");
        req.setPassword("password123");
        req.setConfirmPassword("password123");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("Đăng ký thành công. Vui lòng đăng nhập.")));

        User user = userRepository.findByEmail("testregister@gmail.com").get();
        assert user.getFullName().equals("Nguyễn Văn Test");
    }

    @Test
    void testLogin_Success() throws Exception {
        User user = new User();
        user.setEmail("testlogin@gmail.com");
        user.setFullName("User Login");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setActive(true);
        user.setProvider("LOCAL");
        user.setRole("USER");
        userRepository.save(user);

        LoginRequest req = new LoginRequest();
        req.setEmail("testlogin@gmail.com");
        req.setPassword("password123");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Đăng nhập thành công")))
                .andExpect(cookie().exists("access_token"))
                .andExpect(cookie().exists("refresh_token"));
    }

    @Test
    void testLogin_Fail_WrongPassword() throws Exception {
        User user = new User();
        user.setEmail("testlogin2@gmail.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setActive(true);
        user.setProvider("LOCAL");
        user.setRole("USER");
        userRepository.save(user);

        LoginRequest req = new LoginRequest();
        req.setEmail("testlogin2@gmail.com");
        req.setPassword("wrongpass");

        // GlobalExceptionHandler will catch BadCredentialsException and return 401
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser@gmail.com", authorities = {"USER"})
    void testGetCurrentUser_Success() throws Exception {
        User user = new User();
        user.setEmail("testuser@gmail.com");
        user.setFullName("Current User");
        user.setActive(true);
        user.setProvider("LOCAL");
        user.setRole("USER");
        userRepository.save(user);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/auth/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email", is("testuser@gmail.com")))
                .andExpect(jsonPath("$.user.fullName", is("Current User")));
    }

    @Test
    @WithMockUser(username = "testupdate@gmail.com", authorities = {"USER"})
    void testUpdateProfile_Success() throws Exception {
        User user = new User();
        user.setEmail("testupdate@gmail.com");
        user.setFullName("Old Name");
        user.setActive(true);
        user.setProvider("LOCAL");
        user.setRole("USER");
        userRepository.save(user);

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setFullName("New Name");
        req.setPhoneNumber("0987654321");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/auth/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Cập nhật thông tin thành công")));

        User updated = userRepository.findByEmail("testupdate@gmail.com").get();
        assert updated.getFullName().equals("New Name");
        assert updated.getPhoneNumber().equals("0987654321");
    }

    @Test
    @WithMockUser(username = "testuser@gmail.com")
    void testLogout_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("access_token", 0)) // Cookie should be deleted
                .andExpect(cookie().maxAge("refresh_token", 0));
    }
}
