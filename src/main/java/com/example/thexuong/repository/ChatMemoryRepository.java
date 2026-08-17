package com.example.thexuong.repository;

import com.example.thexuong.entity.ChatMemory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMemoryRepository extends JpaRepository<ChatMemory, String> {
    Page<ChatMemory> findByChatIdContainingIgnoreCase(String chatId, Pageable pageable);
}
