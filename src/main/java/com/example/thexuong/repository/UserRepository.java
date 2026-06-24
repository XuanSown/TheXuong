package com.example.thexuong.repository;

import com.example.thexuong.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Tìm user bằng ID — đơn giản, không cần fetch quan hệ (đã bỏ roles/roleGroups).
    Optional<User> findWithRolesById(Long id);

    // Tìm user bằng username (dùng cho login thường).
    Optional<User> findByUsername(String username);

    // Tìm user bằng email (dùng cho login Google + trang profile).
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    /**
     * Tìm user bằng email — dùng cho Spring Security UserDetailsService.
     * KHÔNG cần @EntityGraph vì User giờ chỉ có cột role String đơn (không còn Set&lt;Role&gt;).
     */
    Optional<User> findWithRolesByEmail(String email);

    /**
     * Tìm user bằng username — dùng cho Spring Security UserDetailsService fallback.
     */
    Optional<User> findWithRolesByUsername(String username);

    /**
     * Load toàn bộ danh sách User theo ID tăng dần (dùng cho trang Admin quản lý user).
     */
    List<User> findAllByOrderByIdAsc();
<<<<<<< HEAD
=======

    /** Đếm số User thuộc một RoleGroup — dùng để kiểm tra trước khi xóa RoleGroup. */
    long countByRoleGroups_Id(Long roleGroupId);

    /**
     * Batch 4: User VIP có tier_promoted_at <= threshold (để cron re-evaluate).
     * Dùng cho TierReevaluateService.reevaluateAllActiveVip().
     */
    List<User> findByTierCodeAndTierPromotedAtBefore(String tierCode, java.time.LocalDateTime before);
>>>>>>> feat/batch-4-tier-vip
}
