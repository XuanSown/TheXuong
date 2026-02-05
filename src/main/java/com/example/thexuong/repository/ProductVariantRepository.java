package com.example.thexuong.repository;

import com.example.thexuong.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    // 4. Thống kê tồn kho theo tên sản phẩm
    // Trả về: [Tên sản phẩm, Tổng số lượng tồn của tất cả các size]
    @Query(value = "SELECT p.name, SUM(v.quantity) " +
            "FROM ProductVariants v " +
            "JOIN Products p ON v.product_id = p.id " +
            "GROUP BY p.name", nativeQuery = true)
    List<Object[]> getInventoryStatistics();

    List<ProductVariant> findByProductId(Long productId);
    Optional<ProductVariant> findByProductIdAndSizeId(Long productId, Long sizeId);
}
