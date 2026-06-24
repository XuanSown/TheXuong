package com.example.thexuong.repository;

import com.example.thexuong.entity.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho Voucher catalog (admin CRUD + user redeem lookup).
 *
 * Methods:
 * - findAllByStatus: filter theo status enum
 * - findByCode: tìm voucher theo code (UNIQUE)
 * - existsByCode: check trùng code trước khi tạo
 * - search: full-text search theo code + filter status/VIP/points
 */
@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    List<Voucher> findAllByStatus(Voucher.Status status);

    Optional<Voucher> findByCode(String code);

    boolean existsByCode(String code);

    /**
     * Search vouchers với filter (admin list page).
     * - search: LIKE trên code (case-insensitive)
     * - status: filter theo Voucher.Status enum (null = bỏ qua)
     * - vipOnly: filter theo vipOnly flag (null = bỏ qua)
     * - minPoints / maxPoints: filter theo requiredPoints range (null = bỏ qua)
     */
    @Query("SELECT v FROM Voucher v WHERE " +
           "(:search IS NULL OR LOWER(v.code) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR v.status = :status) AND " +
           "(:vipOnly IS NULL OR v.vipOnly = :vipOnly) AND " +
           "(:minPoints IS NULL OR v.requiredPoints >= :minPoints) AND " +
           "(:maxPoints IS NULL OR v.requiredPoints <= :maxPoints)")
    Page<Voucher> search(@Param("search") String search,
                         @Param("status") Voucher.Status status,
                         @Param("vipOnly") Boolean vipOnly,
                         @Param("minPoints") Integer minPoints,
                         @Param("maxPoints") Integer maxPoints,
                         Pageable pageable);

    /** Count theo status (cho stats). */
    long countByStatus(Voucher.Status status);

    /** Count theo vipOnly (cho stats). */
    long countByVipOnly(Boolean vipOnly);
}
