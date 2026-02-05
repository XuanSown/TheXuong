package com.example.thexuong.repository;

import com.example.thexuong.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // SELECT * FROM product ORDER BY id DESC LIMIT 4
    List<Product> findTop4ByOrderByIdDesc();
    Page<Product> findByNameContaining(String keyword, Pageable pageable);

    Page<Product> findBySport(String sport, Pageable pageable);
    Page<Product> findByBrand(String brand, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.variants v LEFT JOIN FETCH v.size LEFT JOIN FETCH p.reviews WHERE p.id = :id")
    Optional<Product> findByIdWithVariants(@Param("id") Long id);
}
