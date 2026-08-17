package com.example.thexuong.dto.customercare;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Chi tiết conversation: historyJson đã parse an toàn và transform thành
 * messages [{role, content}] (1 lượt {user,bot} → 2 message).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminConversationDetailResponse {
    private String chatId;
    private LocalDateTime updatedAt;
    private List<AdminChatMessage> messages;
    private boolean parseError;
}
