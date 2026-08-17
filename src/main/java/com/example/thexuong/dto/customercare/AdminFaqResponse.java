package com.example.thexuong.dto.customercare;

import com.example.thexuong.entity.Faq;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response FAQ cho Admin.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminFaqResponse {
    private Long id;
    private String topic;
    private String questionKeywords;
    private String answer;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AdminFaqResponse fromEntity(Faq faq) {
        return AdminFaqResponse.builder()
                .id(faq.getId())
                .topic(faq.getTopic())
                .questionKeywords(faq.getQuestionKeywords())
                .answer(faq.getAnswer())
                .createdAt(faq.getCreatedAt())
                .updatedAt(faq.getUpdatedAt())
                .build();
    }
}
