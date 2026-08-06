package com.example.thexuong.controller;

import com.example.thexuong.entity.Product;
import com.example.thexuong.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AdminProductRestControllerTest extends BaseIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        // Xóa hết dữ liệu test cũ để đảm bảo isolation
        productRepository.deleteAll();
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void testGetProducts_Empty() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/admin/products")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)));
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void testCreateProduct_Success() throws Exception {
        // Giả lập CloudflareR2Service trả về URL ảo
        when(r2Service.uploadMultiple(any())).thenReturn(List.of("http://mock-image.com/img1.jpg"));

        // Mô phỏng request multipart/form-data
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/admin/products")
                .param("name", "Giày thể thao test")
                .param("description", "Mô tả giày test")
                .param("price", "500000")
                .param("imageUrl", "http://mock-image.com/img1.jpg")
                // Không gửi file, chỉ dùng imageUrl mặc định để qua validation r2
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("Tạo sản phẩm thành công")))
                .andExpect(jsonPath("$.product.name", is("Giày thể thao test")))
                .andExpect(jsonPath("$.product.price").value(500000));

        // Verify Database
        List<Product> products = productRepository.findAll();
        assert products.size() == 1;
        assert products.get(0).getName().equals("Giày thể thao test");
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void testUpdateProduct_Success() throws Exception {
        // Tạo sẵn 1 product
        Product product = new Product();
        product.setName("Sản phẩm cũ");
        product.setDescription("Mô tả cũ");
        product.setPrice(new BigDecimal("100000"));
        product.setImageUrl("old.jpg");
        product.setActive(true);
        productRepository.save(product);

        when(r2Service.uploadMultiple(any())).thenReturn(List.of("new.jpg"));

        // Put request (Using multipart is tricky with PUT in Spring test, so we use PATCH/PUT mapping if supported or multipart with HttpMethod.PUT)
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/admin/products/" + product.getId())
                .with(request -> { request.setMethod("PUT"); return request; })
                .param("name", "Sản phẩm mới")
                .param("description", "Mô tả mới")
                .param("price", "200000")
                .param("imageUrl", "new.jpg")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product.name", is("Sản phẩm mới")));

        Product updated = productRepository.findById(product.getId()).get();
        assert updated.getName().equals("Sản phẩm mới");
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void testDeleteProduct_Success() throws Exception {
        Product product = new Product();
        product.setName("Sản phẩm xóa");
        product.setDescription("Mô tả xóa");
        product.setPrice(new BigDecimal("100000"));
        product.setImageUrl("del.jpg");
        product.setActive(true);
        productRepository.save(product);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/admin/products/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Xóa sản phẩm thành công")));

        assert productRepository.findById(product.getId()).isEmpty();
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void testCreateProduct_Forbidden_ForUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/admin/products")
                .param("name", "Giày thể thao test")
                .param("description", "Mô tả giày test")
                .param("price", "500000"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void testCreateProduct_MissingRequiredFields() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/admin/products")
                .param("name", "") // Thiếu tên
                .param("description", "Mô tả giày test")
                .param("price", "500000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Tên sản phẩm là bắt buộc")));
    }
}
