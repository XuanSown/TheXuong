package com.example.thexuong.repository;

import com.example.thexuong.entity.PointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    List<PointTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT pt FROM PointTransaction pt " +
            "WHERE pt.type = com.example.thexuong.entity.PointTransaction.Type.EARN " +
            "AND pt.expiresAt IS NOT NULL " +
            "AND pt.expiresAt < :now")
    List<PointTransaction> findExpiredEarnTransactions(@Param("now") LocalDateTime now);

    @Query("SELECT pt FROM PointTransaction pt " +
            "WHERE pt.orderId = :orderId " +
            "AND pt.type = com.example.thexuong.entity.PointTransaction.Type.EARN")
    List<PointTransaction> findEarnTransactionsByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT pt.type, COUNT(pt), COALESCE(SUM(CAST(pt.points AS long)), 0) " +
            "FROM PointTransaction pt " +
            "GROUP BY pt.type")
    List<Object[]> sumPointsByType();

    @Query("SELECT pt.userId, SUM(CAST(pt.points AS long)) as total " +
            "FROM PointTransaction pt " +
            "WHERE pt.type = com.example.thexuong.entity.PointTransaction.Type.EARN " +
            "GROUP BY pt.userId " +
            "ORDER BY total DESC")
    List<Object[]> topUsersByEarnedPoints(org.springframework.data.domain.Pageable pageable);
<<<<<<< HEAD
=======

    /**
     * Batch 4: Tổng điểm earn của user sau 1 ngày cụ thể.
     * Dùng cho PointTierService tính điểm 365 ngày (Phương án C).
     */
    @Query("SELECT COALESCE(SUM(CAST(pt.points AS long)), 0) FROM PointTransaction pt " +
            "WHERE pt.userId = :userId " +
            "AND pt.type = :type " +
            "AND pt.createdAt >= :since")
    Long sumPointsByUserAndTypeSince(@Param("userId") Long userId,
                                      @Param("type") com.example.thexuong.entity.PointTransaction.Type type,
                                      @Param("since") java.time.LocalDateTime since);
>>>>>>> feat/batch-4-tier-vip
}
