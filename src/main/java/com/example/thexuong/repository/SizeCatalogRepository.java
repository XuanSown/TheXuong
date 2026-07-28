package com.example.thexuong.repository;

import com.example.thexuong.entity.SizeCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SizeCatalogRepository extends JpaRepository<SizeCatalog, Long> {
    List<SizeCatalog> findBySizeTypeIdAndActiveTrueOrderByDisplayOrderAsc(Long sizeTypeId);
    List<SizeCatalog> findByActiveTrueOrderByDisplayOrderAsc();
}
