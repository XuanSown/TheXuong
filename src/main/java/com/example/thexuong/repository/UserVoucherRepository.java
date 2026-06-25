package com.example.thexuong.repository;

import com.example.thexuong.entity.UserVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho UserVoucher.
 * Dùng cho:
 * - Lấy voucher UNUSED của user (UI /loyalty/redeem, /my-vouchers, /checkout widget)
 * - Validate mã khi checkout
 * - Cron expire (query UNUSED + expires_at < now)
 */
@Repository
public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {

    List<UserVoucher> findByUserIdAndStatus(Long userId, UserVoucher.Status status);

    /** Tất cả voucher của user (cả 3 trạng thái), sắp xếp mới nhất trước. */
    List<UserVoucher> findByUserIdOrderByIssuedAtDesc(Long userId);

    Optional<UserVoucher> findByCode(String code);

    /** Cron expire: UNUSED + expires_at < now. */
    @Query("SELECT uv FROM UserVoucher uv " +
            "WHERE uv.status = com.example.thexuong.entity.UserVoucher.Status.UNUSED " +
            "AND uv.expiresAt < :now")
    List<UserVoucher> findExpiredUnusedVouchers(@Param("now") LocalDateTime now);

    /** Đếm số UserVoucher đã claim cho 1 catalog voucher. */
    long countByVoucherId(Long voucherId);

    /** Lấy các voucher UNUSED sắp hết hạn trong khoảng thời gian (để gửi email cảnh báo). */
    @Query("SELECT uv FROM UserVoucher uv " +
            "WHERE uv.status = com.example.thexuong.entity.UserVoucher.Status.UNUSED " +
            "AND uv.expiresAt BETWEEN :from AND :to")
    List<UserVoucher> findExpiringSoonVouchers(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
