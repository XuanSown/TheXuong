package com.example.thexuong.repository;

import com.example.thexuong.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT SUM(o.totalMoney) FROM Order o WHERE o.status = 'DELIVERED'")
    Double calculateTotalRevenue();
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countOrdersByStatus();

    List<Order> findAllByOrderByCreatedAtDesc();

}