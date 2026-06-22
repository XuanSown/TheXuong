package com.example.thexuong.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity User - Người dùng hệ thống.
 * QUAN TRỌNG: Dùng @Getter/@Setter thay vì @Data để tránh lỗi StackOverflowError
 * do @ManyToMany với RoleGroup tạo vòng lặp vô tận trong toString()/hashCode().
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
// Chỉ dùng 'id' để equals/hashCode — tránh đệ quy qua roles, roleGroup
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
// Loại bỏ các trường quan hệ khỏi toString() để tránh LazyInitializationException
@ToString(exclude = {"roles", "roleGroups", "password"})
@Entity
@Table(name = "Users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "username", unique = true, columnDefinition = "NVARCHAR(255)")
    private String username;

    private String password;

    @Column(name = "full_name", columnDefinition = "NVARCHAR(255)")
    private String fullName;

    @Column(name = "provider_id")
    private String providerId;

    @Column(unique = true, nullable = false)
    private String email;

    @Builder.Default
    private String provider = "LOCAL"; // 'LOCAL' hoặc 'GOOGLE'

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String address;

    /**
     * Trạng thái hoạt động: true = Active, false = Bị khóa.
     * Khi false, Spring Security sẽ từ chối đăng nhập (UserDetails.isEnabled() = false).
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    /**
     * Chức danh (VD: Giám đốc, Quản lý kho, Khách hàng).
     * 1 User có thể thuộc nhiều RoleGroups (N-N).
     * LAZY để tránh query thừa khi chỉ cần thông tin cơ bản của User.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_role_groups",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_group_id")
    )
    @Builder.Default
    private Set<RoleGroup> roleGroups = new HashSet<>();

    /**
     * Các quyền riêng (cá biệt) của User, override/bổ sung ngoài quyền từ RoleGroup.
     * VD: Nhân viên kho nhưng được cấp thêm quyền HR đặc cách.
     * LAZY — dùng @EntityGraph khi cần load đầy đủ trong SecurityContext.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    // ============================================================
    // Batch 4: Tier fields (Phương án C + Y)
    // ============================================================

    /**
     * Hạng thành viên: 'THUONG' / 'VIP' (FK semantic tới PointTiers.code, không có FK constraint vì PointTiers chưa có sẵn).
     * Set khi user đặt đơn đầu tiên (OrderService.placeOrder) hoặc khi nâng/hạ hạng.
     */
    @Column(name = "tier_code", length = 20)
    private String tierCode;

    /** Thời điểm lên hạng gần nhất — dùng cho logic re-evaluate 365 ngày (Phương án Y). */
    @Column(name = "tier_promoted_at")
    private java.time.LocalDateTime tierPromotedAt;
}
