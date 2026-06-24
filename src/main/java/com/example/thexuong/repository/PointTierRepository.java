package com.example.thexuong.repository;

import com.example.thexuong.entity.PointTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho PointTier.
 * Thường chỉ có 2 row (THUONG + VIP), nhưng để API mở rộng thêm tier sau này.
 */
@Repository
public interface PointTierRepository extends JpaRepository<PointTier, Long> {

    Optional<PointTier> findByCode(String code);

    /** Lấy tất cả tier sắp xếp theo min_total_spent tăng dần. */
    List<PointTier> findAllByOrderByMinTotalSpentAsc();
}
