package com.example.thexuong.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Lightweight product DTO for cart recommendations.
 * Chỉ chứa các field ProductCard frontend cần, không load images/variants.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationProductDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private String imageUrl;
    private String sport;
    private String brand;
    private String category;
}
