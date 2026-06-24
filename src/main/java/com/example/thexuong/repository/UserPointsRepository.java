package com.example.thexuong.repository;

import com.example.thexuong.entity.UserPoints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho UserPoints.
 * KHÔNG dùng @Lock(OPTIMISTIC) ở method level — @Version trên entity tự xử lý optimistic lock.
 */
@Repository
public interface UserPointsRepository extends JpaRepository<UserPoints, Long> {
    Optional<UserPoints> findByUserId(Long userId);
}
