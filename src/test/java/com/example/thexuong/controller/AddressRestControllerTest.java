package com.example.thexuong.controller;

import com.example.thexuong.dto.address.AddressRequest;
import com.example.thexuong.entity.UserAddress;
import com.example.thexuong.entity.User;
import com.example.thexuong.repository.UserAddressRepository;
import com.example.thexuong.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AddressRestControllerTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAddressRepository userAddressRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long testAddressId;

    @BeforeEach
    void setUp() {
        userAddressRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setEmail("address@gmail.com");
        user.setFullName("Address User");
        user.setPassword("pass");
        user.setRole("USER");
        userRepository.save(user);

        UserAddress addr = new UserAddress();
        addr.setUser(user);
        addr.setRecipientName("Nguyễn Khách");
        addr.setRecipientPhone("0999999999");
        addr.setProvinceCode("01");
        addr.setDistrictCode("001");
        addr.setWardCode("00001");
        addr.setStreetDetail("Số 1 Ngõ 2");
        addr.setIsDefault(true);
        userAddressRepository.save(addr);

        testAddressId = addr.getId();
    }

    @Test
    @WithMockUser(username = "address@gmail.com")
    void testListAddresses() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].recipientName", is("Nguyễn Khách")));
    }

    @Test
    @WithMockUser(username = "address@gmail.com")
    void testCreateAddress() throws Exception {
        AddressRequest req = new AddressRequest();
        req.setRecipientName("Tên Mới");
        req.setRecipientPhone("0987654321");
        req.setProvinceCode("79");
        req.setDistrictCode("760");
        req.setWardCode("26734");
        req.setStreetDetail("Đường abc");
        req.setIsDefault(false);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/addresses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipientName", is("Tên Mới")));
    }

    @Test
    @WithMockUser(username = "address@gmail.com")
    void testSetDefault() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/addresses/" + testAddressId + "/default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Đặt địa chỉ mặc định thành công")));
    }

    @Test
    @WithMockUser(username = "address@gmail.com")
    void testDeleteAddress() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/addresses/" + testAddressId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Xóa địa chỉ thành công")));
    }
}
