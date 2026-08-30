package com.example.thexuong.repository;

import com.example.thexuong.entity.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    List<Order> findByUserIdOrderByIdDesc(Long userId);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderDetails WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderDetails WHERE o.id = :id AND o.phoneNumber = :phoneNumber")
    Optional<Order> findByIdAndPhoneNumberWithDetails(@Param("id") Long id, @Param("phoneNumber") String phoneNumber);

    // 3. Tổng doanh thu theo ngày (có thể lọc theo khoảng thời gian)
    // Trả về: [Ngày (yyyy-MM-dd), Tổng tiền]
    // Task 0.9: Sửa 'SHIPPED' → 'COMPLETED' (chuẩn hoá theo OrderStatus enum mới)
    @Query(value = "SELECT CAST(created_at AS DATE) as order_date, SUM(total_money) " +
            "FROM Orders " +
            "WHERE status = 'COMPLETED' " +
            "AND (:startDate IS NULL OR CAST(created_at AS DATE) >= :startDate) " +
            "AND (:endDate IS NULL OR CAST(created_at AS DATE) <= :endDate) " +
            "GROUP BY CAST(created_at AS DATE) " +
            "ORDER BY order_date DESC", nativeQuery = true)
    List<Object[]> getRevenueByDay(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // 1. Thống kê số lượng đơn hàng theo từng trạng thái (có thể lọc theo khoảng thời gian)
    @Query("SELECT o.status, COUNT(o) FROM Order o " +
            "WHERE (:startDate IS NULL OR o.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR o.createdAt <= :endDate) " +
            "GROUP BY o.status")
    List<Object[]> countOrdersByStatus(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // 2. Đếm số lượng User (đã đăng ký) CÓ thực hiện mua hàng (có đơn hàng) trong khoảng thời gian
    @Query("SELECT COUNT(DISTINCT o.user.id) FROM Order o " +
            "WHERE o.user IS NOT NULL " +
            "AND (:startDate IS NULL OR o.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR o.createdAt <= :endDate)")
    long countUsersWithOrders(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // 4. Đếm tổng số đơn hàng trong khoảng thời gian
    @Query("SELECT COUNT(o) FROM Order o " +
            "WHERE (:startDate IS NULL OR o.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR o.createdAt <= :endDate)")
    long countOrdersByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // 5. Tổng doanh thu trong khoảng thời gian (chỉ tính đơn đã giao - COMPLETED)
    // Task 0.9: Sửa 'SHIPPED, DELIVERED' → 'COMPLETED' (chuẩn hoá theo OrderStatus enum)
    @Query(value = "SELECT COALESCE(SUM(total_money), 0) FROM Orders " +
            "WHERE status = 'COMPLETED' " +
            "AND (:startDate IS NULL OR CAST(created_at AS DATE) >= :startDate) " +
            "AND (:endDate IS NULL OR CAST(created_at AS DATE) <= :endDate)", nativeQuery = true)
    Double getTotalRevenue(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Batch 4: Tổng total_for_point_calc của user từ đơn COMPLETED, sau 1 ngày cụ thể.
     * Dùng cho PointTierService tính chi tiêu 365 ngày (Phương án C).
     */
    @Query("SELECT COALESCE(SUM(o.totalForPointCalc), 0) FROM Order o " +
            "WHERE o.user.id = :userId " +
            "AND o.status = com.example.thexuong.entity.OrderStatus.COMPLETED " +
            "AND o.completedAt >= :since")
    java.math.BigDecimal sumTotalForPointCalcByUserSince(@Param("userId") Long userId,
                                                        @Param("since") java.time.LocalDateTime since);

    /**
     * Batch 0: REST API - lấy tất cả đơn của user kèm orderDetails.
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderDetails WHERE o.user.id = :userId ORDER BY o.id DESC")
    List<Order> findByUserIdWithDetails(@Param("userId") Long userId);
}
