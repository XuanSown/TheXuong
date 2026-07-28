package com.example.thexuong.repository;

import com.example.thexuong.entity.TierHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TierHistoryRepository extends JpaRepository<TierHistory, Long> {
}
