package com.example.thexuong.repository;

import com.example.thexuong.entity.ChatLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatLogRepository extends JpaRepository<ChatLog, Long> {
    List<ChatLog> findByChatIdOrderByCreatedAtDesc(String chatId);
}
