package com.example.thexuong.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ChatbotOrderDto {
    private Long orderId;
    private String status;
    private BigDecimal totalMoney;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private List<String> items;
}
