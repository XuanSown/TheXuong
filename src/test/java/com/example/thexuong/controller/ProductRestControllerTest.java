package com.example.thexuong.controller;

import com.example.thexuong.entity.Brand;
import com.example.thexuong.entity.Product;
import com.example.thexuong.entity.Sport;
import com.example.thexuong.repository.BrandRepository;
import com.example.thexuong.repository.ProductRepository;
import com.example.thexuong.repository.SportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ProductRestControllerTest extends BaseIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SportRepository sportRepository;

    @Autowired
    private BrandRepository brandRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        sportRepository.deleteAll();
        brandRepository.deleteAll();
    }

    @Test
    void testGetProducts_PublicAccess() throws Exception {
        // Setup data
        Sport s = new Sport();
        s.setName("Bóng Đá");
        sportRepository.save(s);

        Brand b = new Brand();
        b.setName("Nike");
        brandRepository.save(b);

        Product p = new Product();
        p.setName("Giày public");
        p.setPrice(new BigDecimal("100000"));
        p.setActive(true);
        p.setSport(s);
        p.setBrand(b);
        productRepository.save(p);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Giày public")));
    }

    @Test
    void testGetProductDetail() throws Exception {
        Product p = new Product();
        p.setName("Áo thun");
        p.setPrice(new BigDecimal("150000"));
        p.setActive(true);
        p.setViewCount(0);
        productRepository.save(p);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products/" + p.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Áo thun")));

        // View count should be increased
        Product updated = productRepository.findById(p.getId()).get();
        assert updated.getViewCount() == 1;
    }

    @Test
    void testGetNewProducts() throws Exception {
        for (int i = 0; i < 3; i++) {
            Product p = new Product();
            p.setName("Sản phẩm mới " + i);
            p.setPrice(new BigDecimal("100000"));
            p.setActive(true);
            productRepository.save(p);
        }

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products/new")
                .param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}
