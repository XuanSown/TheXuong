package com.example.thexuong.repository;

import com.example.thexuong.entity.ChatLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatLogRepository extends JpaRepository<ChatLog, Long>, JpaSpecificationExecutor<ChatLog> {
    List<ChatLog> findByChatIdOrderByCreatedAtDesc(String chatId);

    long countByCreatedAtAfter(LocalDateTime start);

    @Query("SELECT c.intent, COUNT(c) FROM ChatLog c " +
            "WHERE c.intent IS NOT NULL AND c.intent <> '' " +
            "GROUP BY c.intent ORDER BY COUNT(c) DESC")
    List<Object[]> findTopIntents(Pageable pageable);
}
