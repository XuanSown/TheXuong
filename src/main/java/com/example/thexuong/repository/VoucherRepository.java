package com.example.thexuong.repository;

import com.example.thexuong.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho Voucher catalog.
 * Admin CRUD + user redeem lookup.
 */
@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long>, JpaSpecificationExecutor<Voucher> {
    List<Voucher> findAllByStatus(Voucher.Status status);
    Optional<Voucher> findByCode(String code);
    boolean existsByCode(String code);

    // Stats helper: count by status
    long countByStatus(Voucher.Status status);

    // Stats: count VIP-only vouchers
    long countByVipOnlyTrue();
}
