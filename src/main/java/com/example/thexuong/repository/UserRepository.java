package com.example.thexuong.repository;

import com.example.thexuong.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = {"roles", "roleGroups"})
    Optional<User> findWithRolesById(Long id);

    //tìm user bằng username (dùng cho login thường)
    Optional<User> findByUsername(String username);

    //tìm user bằng email (dùng cho login gg)
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    /**
     * Tìm user bằng email và EAGER fetch roles + roleGroup.roles trong 1 query duy nhất.
     * Sử dụng cho Spring Security UserDetailsService để tránh N+1 Query.
     */
    @EntityGraph(attributePaths = {"roles", "roleGroups", "roleGroups.roles"})
    Optional<User> findWithRolesByEmail(String email);

    /**
     * Tìm user bằng username và EAGER fetch roles + roleGroup.roles.
     */
    @EntityGraph(attributePaths = {"roles", "roleGroups", "roleGroups.roles"})
    Optional<User> findWithRolesByUsername(String username);

    /**
     * Load toàn bộ danh sách User kèm roleGroup (cho trang Admin quản lý user).
     * Chỉ fetch roleGroup (không fetch roles để tránh Cartesian Product trên danh sách lớn).
     */
    @EntityGraph(attributePaths = {"roleGroups"})
    List<User> findAllByOrderByIdAsc();

    /** Đếm số User thuộc một RoleGroup — dùng để kiểm tra trước khi xóa RoleGroup. */
    long countByRoleGroups_Id(Long roleGroupId);

    /**
     * Batch 4: User VIP có tier_promoted_at <= threshold (để cron re-evaluate).
     * Dùng cho TierReevaluateService.reevaluateAllActiveVip().
     */
    List<User> findByTierCodeAndTierPromotedAtBefore(String tierCode, java.time.LocalDateTime before);
}
