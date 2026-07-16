package com.example.thexuong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "chat_logs")
public class ChatLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id", nullable = false, length = 100)
    private String chatId;

    @Column(name = "user_name", columnDefinition = "NVARCHAR(255)")
    private String userName;

    @Column(columnDefinition = "NVARCHAR(50)")
    private String intent;

    @Column(name = "user_message", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String userMessage;

    @Column(name = "bot_reply", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String botReply;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }
}
