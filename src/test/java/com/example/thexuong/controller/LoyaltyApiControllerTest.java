package com.example.thexuong.controller;

import com.example.thexuong.entity.User;
import com.example.thexuong.entity.Voucher;
import com.example.thexuong.repository.UserRepository;
import com.example.thexuong.repository.VoucherRepository;
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

public class LoyaltyApiControllerTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private com.example.thexuong.repository.UserPointsRepository userPointsRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long testVoucherId;

    @BeforeEach
    void setUp() {
        userPointsRepository.deleteAll();
        voucherRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setEmail("loyalty@gmail.com");
        user.setUsername("loyalty@gmail.com");
        user.setFullName("Loyalty User");
        user.setPassword("pass");
        user.setRole("USER");
        userRepository.save(user);

        com.example.thexuong.entity.UserPoints up = new com.example.thexuong.entity.UserPoints();
        up.setUser(user);
        up.setCurrentPoints(100);
        up.setTotalEarned(100L);
        up.setTotalSpent(0L);
        up.setLastActivityAt(java.time.LocalDateTime.now());
        userPointsRepository.save(up);

        Voucher v = new Voucher();
        v.setCode("TEST10K");
        v.setDiscountAmount(new BigDecimal("10000"));
        v.setRequiredPoints(10);
        v.setStatus(Voucher.Status.ACTIVE);
        v.setMinOrderAmount(BigDecimal.ZERO);
        voucherRepository.save(v);

        testVoucherId = v.getId();
    }

    @Test
    @WithMockUser(username = "loyalty@gmail.com")
    void testGetPoints() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/loyalty/points"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentPoints", is(100)));
    }

    @Test
    @WithMockUser(username = "loyalty@gmail.com")
    void testGetCatalog() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/loyalty/catalog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @WithMockUser(username = "loyalty@gmail.com")
    void testRedeemVoucher() throws Exception {
        Map<String, Object> req = Map.of("voucherId", testVoucherId);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/loyalty/redeem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Đổi voucher thành công.")));
    }

    @Test
    @WithMockUser(username = "loyalty@gmail.com")
    void testGetMyVouchers() throws Exception {
        Map<String, Object> req = Map.of("voucherId", testVoucherId);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/loyalty/redeem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/my-vouchers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
    }
}
