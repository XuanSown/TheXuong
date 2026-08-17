package com.example.thexuong.dto.customercare;

import com.example.thexuong.entity.ChatLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response chat log cho Admin (chỉ đọc — không cho phép sửa).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminChatLogResponse {
    private Long id;
    private String chatId;
    private String userName;
    private String intent;
    private String userMessage;
    private String botReply;
    private LocalDateTime createdAt;

    public static AdminChatLogResponse fromEntity(ChatLog log) {
        return AdminChatLogResponse.builder()
                .id(log.getId())
                .chatId(log.getChatId())
                .userName(log.getUserName())
                .intent(log.getIntent())
                .userMessage(log.getUserMessage())
                .botReply(log.getBotReply())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
