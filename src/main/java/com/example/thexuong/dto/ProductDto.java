package com.example.thexuong.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Product DTO for REST API responses (public-facing).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String imageUrl;    // ảnh chính
    private List<String> images; // danh sách tất cả ảnh (1-5 ảnh)
    private String sport;
    private String brand;
    private String category;
    private Integer viewCount;
    private Integer stockQuantity;
private List<SizeDto> sizes; // danh sách size và số lượng tồn kho
}
