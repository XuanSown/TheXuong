package com.example.thexuong.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request body for POST /api/v1/products/recommendations/cart.
 * Dùng chung cho guest cart và authenticated cart.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartRecommendationRequest {
    private List<Long> productIds;
    private Integer limit;
}
