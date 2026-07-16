package com.example.thexuong.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Simplified product DTO for chatbot context.
 * Contains essential fields for AI to answer product queries.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotProductDto {
    private Long id;
    private String name;
    private String category;
    private Double price;
    private String sport;
    private String brand;
    private String description;
    private String stockStatus; // "Còn hàng" / "Hết hàng" / "Sắp hết"
}
