package com.example.thexuong.repository;

import com.example.thexuong.entity.TierEvaluationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TierEvaluationLogRepository extends JpaRepository<TierEvaluationLog, Long> {

    List<TierEvaluationLog> findByUserIdOrderByEvaluatedAtDesc(Long userId);

    /**
     * User VIP sắp đến hạn re-evaluate (đã promote >= 335 ngày trước, tức còn <= 30 ngày nữa).
     * Dùng cho TierWarningJob (cron daily 09:00).
     */
    @Query("SELECT tel FROM TierEvaluationLog tel " +
            "WHERE tel.evaluatedAt = (" +
            "  SELECT MAX(t2.evaluatedAt) FROM TierEvaluationLog t2 WHERE t2.userId = tel.userId" +
            ") " +
            "AND tel.newTierCode = 'VIP' " +
            "AND tel.evaluatedAt <= :before")
    List<TierEvaluationLog> findUsersNearReevaluation(@Param("before") LocalDateTime before);
}
