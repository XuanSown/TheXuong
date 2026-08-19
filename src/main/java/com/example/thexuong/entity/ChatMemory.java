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
@Table(name = "chat_memory")
public class ChatMemory {

    @Id
    @Column(name = "chat_id", nullable = false, length = 100)
    private String chatId;

    @Column(name = "history_json", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String historyJson;

    @Column(name = "state_json", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String stateJson;

    @Column(name = "updated_at", nullable = false)
    private java.time.LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }
}
