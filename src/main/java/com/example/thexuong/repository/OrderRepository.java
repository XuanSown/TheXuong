package com.example.thexuong.repository;

import com.example.thexuong.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByIdDesc(Long userId);
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderDetails WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);
}
