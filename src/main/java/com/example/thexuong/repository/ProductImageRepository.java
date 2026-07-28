package com.example.thexuong.repository;

import com.example.thexuong.entity.ProductImage;
import com.example.thexuong.entity.ProductImageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, ProductImageId> {
    List<ProductImage> findByProductIdOrderBySortOrderAsc(Long productId);
    void deleteByProductId(Long productId);
}
