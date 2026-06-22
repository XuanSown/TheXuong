package com.example.thexuong.repository;

import com.example.thexuong.entity.PointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository cho PointTransaction.
 * Dùng cho:
 * - Lịch sử giao dịch của user (UI loyalty)
 * - Cron expire điểm (query EARN có expires_at < now và chưa REVERSE/EXPIRE)
 * - Admin report (countByType, sumPointsByUserId)
 */
@Repository
public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    /** Lịch sử giao dịch của user, mới nhất trước. */
    List<PointTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Tìm EARN transactions quá hạn (expires_at < now) mà chưa có REVERSE/EXPIRE tương ứng.
     * Dùng cho cron expire daily.
     * Logic: lấy tất cả EARN có expires_at < now, check xem có EARN_REVERSE không (orderId + userId).
     * Đơn giản hoá: query EARN có expires_at < now, service sẽ loop check REVERSE tương ứng.
     */
    @Query("SELECT pt FROM PointTransaction pt " +
            "WHERE pt.type = com.example.thexuong.entity.PointTransaction.Type.EARN " +
            "AND pt.expiresAt IS NOT NULL " +
            "AND pt.expiresAt < :now")
    List<PointTransaction> findExpiredEarnTransactions(@Param("now") LocalDateTime now);

    /** Tìm EARN transactions theo orderId (để reverse khi refund). */
    @Query("SELECT pt FROM PointTransaction pt " +
            "WHERE pt.orderId = :orderId " +
            "AND pt.type = com.example.thexuong.entity.PointTransaction.Type.EARN")
    List<PointTransaction> findEarnTransactionsByOrderId(@Param("orderId") Long orderId);

    /** Admin report: tổng điểm đã phát hành theo từng type. */
    @Query("SELECT pt.type, COUNT(pt), COALESCE(SUM(CAST(pt.points AS long)), 0) " +
            "FROM PointTransaction pt " +
            "GROUP BY pt.type")
    List<Object[]> sumPointsByType();

    /** Admin report: top user theo tổng điểm earn. */
    @Query("SELECT pt.userId, SUM(CAST(pt.points AS long)) as total " +
            "FROM PointTransaction pt " +
            "WHERE pt.type = com.example.thexuong.entity.PointTransaction.Type.EARN " +
            "GROUP BY pt.userId " +
            "ORDER BY total DESC")
    List<Object[]> topUsersByEarnedPoints(org.springframework.data.domain.Pageable pageable);
}
