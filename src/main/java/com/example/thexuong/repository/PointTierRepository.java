package com.example.thexuong.repository;

import com.example.thexuong.entity.PointTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PointTierRepository extends JpaRepository<PointTier, Long> {
    Optional<PointTier> findByCode(String code);
    List<PointTier> findAllByOrderByMinTotalSpentAsc();
}
