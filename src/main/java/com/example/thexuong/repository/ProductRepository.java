package com.example.thexuong.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.thexuong.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

	// SELECT * FROM product ORDER BY id DESC LIMIT 4
	List<Product> findTop4ByOrderByIdDesc();

	Page<Product> findByNameContaining(String keyword, Pageable pageable);

	Page<Product> findBySport_Name(String sport, Pageable pageable);

	Page<Product> findByBrand_Name(String brand, Pageable pageable);

	@Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.variants v LEFT JOIN FETCH v.size LEFT JOIN FETCH p.reviews WHERE p.id = :id")
	Optional<Product> findByIdWithVariants(@Param("id") Long id);

	// Task 0.10: Query đã đúng ('COMPLETED' đã có sẵn), chỉ thêm comment.
	// Best-selling products: tính trên đơn COMPLETED.
	@Query("SELECT p, COALESCE(SUM(od.quantity), 0) as daBan "
			+ "FROM Product p "
			+ "LEFT JOIN OrderDetail od ON p.id = od.productId "
			+ "LEFT JOIN od.order o "
			+ "WHERE (o.status = com.example.thexuong.entity.OrderStatus.COMPLETED OR o.status IS NULL OR od.id IS NULL) "
			+ "GROUP BY p.id, p.name, p.price, p.imageUrl, p.category, p.description, p.brand, p.sport "
			+ "ORDER BY daBan DESC")
	List<Object[]> findBestSellingProduct(Pageable pageable);

	@Query("SELECT p, COALESCE(SUM(od.quantity), 0) as daBan "
			+ "FROM Product p "
			+ "LEFT JOIN OrderDetail od ON p.id = od.productId "
			+ "LEFT JOIN od.order o "
			+ "WHERE (o.status = com.example.thexuong.entity.OrderStatus.COMPLETED OR o.status IS NULL OR od.id IS NULL) "
			+ "GROUP BY p.id, p.name, p.price, p.imageUrl, p.category, p.description, p.brand, p.sport "
			+ "ORDER BY daBan ASC")
	List<Object[]> findSlowMovingProducts(Pageable pageable);

	// Lấy top sản phẩm xem nhiều nhất
	@Query("SELECT p.name, COALESCE(p.viewCount, 0) "
			+ "FROM Product p "
			+ "ORDER BY COALESCE(p.viewCount, 0) DESC")
	List<Object[]> findTopViewedProducts(Pageable pageable);

	@Query("SELECT p.name, COALESCE(p.viewCount, 0) "
			+ "FROM Product p "
			+ "ORDER BY COALESCE(p.viewCount, 0) ASC")
	List<Object[]> findLeastViewedProducts(Pageable pageable);

	// API methods
	@Query("SELECT DISTINCT p.sport.name FROM Product p WHERE p.sport IS NOT NULL ORDER BY p.sport.name")
	List<String> findAllDistinctSports();

	@Query("SELECT DISTINCT p.brand.name FROM Product p WHERE p.brand IS NOT NULL ORDER BY p.brand.name")
	List<String> findAllDistinctBrands();

	@Query("SELECT DISTINCT p.category.name FROM Product p WHERE p.category IS NOT NULL ORDER BY p.category.name")
	List<String> findAllDistinctCategories();

	// Find top products by ID desc for new arrivals (with JOIN FETCH to avoid LazyInitializationException)
	@Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.variants v LEFT JOIN FETCH v.size WHERE p.active = true ORDER BY p.id DESC")
	List<Product> findAllWithVariantsByOrderByIdDesc(Pageable pageable);

	// Find top products by ID desc for new arrivals
	List<Product> findAllByOrderByIdDesc(Pageable pageable);

	// ========== ADMIN: Soft Delete / Product Management ==========
	/**
	 * Admin: Lấy tất cả sản phẩm kể cả inactive (đã vô hiệu hóa). Bỏ qua
	 * @SQLRestriction bằng native query.
	 */
	@Query(value = "SELECT * FROM Products", nativeQuery = true)
	List<Product> findAllIncludingInactive();

	@Query(value = "SELECT * FROM Products", countQuery = "SELECT count(*) FROM Products", nativeQuery = true)
	Page<Product> findAllIncludingInactivePageable(Pageable pageable);

	/**
	 * Admin: Tìm kiếm tất cả sản phẩm (bao gồm inactive) theo tên.
	 */
	@Query(value = "SELECT * FROM Products WHERE LOWER(name) LIKE LOWER(CONCAT('%', :keyword, '%'))",
			countQuery = "SELECT count(*) FROM Products WHERE LOWER(name) LIKE LOWER(CONCAT('%', :keyword, '%'))",
			nativeQuery = true)
	Page<Product> findAllIncludingInactiveByNameContainingPageable(@Param("keyword") String keyword, Pageable pageable);

	@Query(value = "SELECT * FROM Products WHERE id = :id", nativeQuery = true)
	Optional<Product> findByIdIncludingInactive(@Param("id") Long id);
}
