package com.example.thexuong.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AdminStatisticsRestControllerTest extends BaseIntegrationTest {

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void testGetStatistics_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/admin/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topSelling").isArray())
                .andExpect(jsonPath("$.slowSelling").isArray())
                .andExpect(jsonPath("$.revenueByDay").isArray())
                .andExpect(jsonPath("$.inventory").isArray())
                .andExpect(jsonPath("$.topViewed").isArray())
                .andExpect(jsonPath("$.leastViewed").isArray())
                .andExpect(jsonPath("$.orderStatusStats").isArray())
                .andExpect(jsonPath("$.totalUsers").isNumber())
                .andExpect(jsonPath("$.usersWithOrders").isNumber())
                .andExpect(jsonPath("$.usersWithoutOrders").isNumber());
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void testGetStatistics_ForbiddenForUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/admin/statistics"))
                .andExpect(status().isForbidden());
    }
}
