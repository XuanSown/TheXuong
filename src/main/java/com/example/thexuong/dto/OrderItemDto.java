package com.example.thexuong.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * OrderItem DTO for REST API.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDto {
    private Long id;
    private String productName;
    private String size;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String imageUrl;
}
