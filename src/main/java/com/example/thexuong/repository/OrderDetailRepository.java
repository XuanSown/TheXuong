package com.example.thexuong.repository;

import com.example.thexuong.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    // 1. Top sản phẩm bán chạy (Sắp xếp giảm dần theo tổng số lượng)
    // Trả về: [Tên sản phẩm, Tổng số lượng đã bán]
    @Query(value = "SELECT product_name, SUM(quantity) as total_sold " +
            "FROM OrderDetails " +
            "GROUP BY product_name " +
            "ORDER BY total_sold DESC", nativeQuery = true)
    List<Object[]> findTopSellingProducts();

    // 2. Top sản phẩm bán chậm (Sắp xếp tăng dần - chỉ tính những SP đã bán được ít nhất 1 lần)
    @Query(value = "SELECT product_name, SUM(quantity) as total_sold " +
            "FROM OrderDetails " +
            "GROUP BY product_name " +
            "ORDER BY total_sold ASC", nativeQuery = true)
    List<Object[]> findSlowSellingProducts();
}
