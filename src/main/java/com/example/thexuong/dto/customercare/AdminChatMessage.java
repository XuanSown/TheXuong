package com.example.thexuong.dto.customercare;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Một message sau khi transform từ cặp {user, bot}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminChatMessage {
    private String role;
    private String content;
}
