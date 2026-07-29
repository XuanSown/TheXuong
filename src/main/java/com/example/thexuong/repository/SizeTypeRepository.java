package com.example.thexuong.repository;

import com.example.thexuong.entity.SizeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SizeTypeRepository extends JpaRepository<SizeType, Long> {
    List<SizeType> findByActiveTrue();
    Optional<SizeType> findByCode(String code);
}
