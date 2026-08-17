package com.example.thexuong.dto.customercare;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Item tóm tắt một conversation (chat_memory) trong danh sách.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminChatMemoryResponse {
    private String chatId;
    private LocalDateTime updatedAt;
    private Integer messageCount;
    private String lastMessage;
}
