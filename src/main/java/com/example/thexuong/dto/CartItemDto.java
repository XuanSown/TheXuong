package com.example.thexuong.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * CartItem DTO for REST API.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDto {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private Long variantId;
    private String size;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
}
