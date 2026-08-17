package com.example.thexuong.repository;

import com.example.thexuong.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LoginHistoryRepository
        extends JpaRepository<LoginHistory, Long>, JpaSpecificationExecutor<LoginHistory> {
}
