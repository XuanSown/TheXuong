package com.example.thexuong.repository;

import com.example.thexuong.entity.OrderDetail;
import com.example.thexuong.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    // 1. Top sản phẩm bán chạy (Sắp xếp giảm dần theo tổng số lượng)
    // Trả về: [Tên sản phẩm, Tổng số lượng đã bán, Tổng doanh thu]
    @Query(value = "SELECT product_name, SUM(quantity) as total_sold, SUM(total_price) as total_revenue " +
            "FROM OrderDetails " +
            "GROUP BY product_name " +
            "ORDER BY total_sold DESC", nativeQuery = true)
    List<Object[]> findTopSellingProducts();

    // 2. Top sản phẩm bán chậm (Sắp xếp tăng dần - chỉ tính những SP đã bán được ít nhất 1 lần)
    @Query(value = "SELECT product_name, SUM(quantity) as total_sold, SUM(total_price) as total_revenue " +
            "FROM OrderDetails " +
            "GROUP BY product_name " +
            "ORDER BY total_sold ASC", nativeQuery = true)
    List<Object[]> findSlowSellingProducts();

    // 3. Kiểm tra user đã mua sản phẩm với trạng thái đơn cụ thể (dùng cho Review).
    @Query("""
            SELECT COUNT(d) > 0 FROM OrderDetail d
            WHERE d.productId = :productId
              AND d.order.user.id = :userId
              AND d.order.status = :status
            """)
    boolean existsPurchaseWithStatus(@Param("productId") Long productId,
                                     @Param("userId") Long userId,
                                     @Param("status") OrderStatus status);
}
