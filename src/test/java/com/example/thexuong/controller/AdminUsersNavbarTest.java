package com.example.thexuong.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Kiểm tra phân quyền giao diện navbar theo role trong trang /admin/users.
 *
 * Yêu cầu nghiệp vụ:
 *  - ADMIN only → chỉ thấy menu admin, KHÔNG thấy menu user (THỂ THAO / giỏ hàng)
 *  - BOTH       → thấy cả hai menu user lẫn admin
 *  - ADMIN+BOTH → dùng giao diện BOTH (thấy cả hai)
 */
@SpringBootTest
@AutoConfigureMockMvc
class AdminUsersNavbarTest {

    @Autowired
    MockMvc mockMvc;

    // ── Bug được fix: ADMIN only phải ẨN menu user ──────────────────────────

    @Test
    @DisplayName("ADMIN only: menu user (THỂ THAO) phải ẨN trong trang /admin/users")
    @WithMockUser(username = "admin@test.com", authorities = {"ADMIN"})
    void adminOnly_shouldNotSeeUserMenu_onAdminUsersPage() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                // Menu THỂ THAO chỉ dành cho USER / BOTH — phải ẩn với ADMIN only
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("sportDropdown"))));
    }

    @Test
    @DisplayName("ADMIN only: menu admin (Sản phẩm) phải HIỆN trong trang /admin/users")
    @WithMockUser(username = "admin@test.com", authorities = {"ADMIN"})
    void adminOnly_shouldSeeAdminMenu_onAdminUsersPage() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/admin/products")));
    }

    // ── BOTH phải thấy cả hai ────────────────────────────────────────────────

    @Test
    @DisplayName("BOTH: menu user (THỂ THAO) phải HIỆN trong trang /admin/users")
    @WithMockUser(username = "both@test.com", authorities = {"BOTH"})
    void both_shouldSeeUserMenu_onAdminUsersPage() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("sportDropdown")));
    }

    @Test
    @DisplayName("ADMIN+BOTH: menu user (THỂ THAO) phải HIỆN (dùng giao diện BOTH)")
    @WithMockUser(username = "adminboth@test.com", authorities = {"ADMIN", "BOTH"})
    void adminAndBoth_shouldSeeUserMenu_onAdminUsersPage() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("sportDropdown")));
    }
}
