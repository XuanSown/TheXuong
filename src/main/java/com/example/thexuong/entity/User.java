package com.example.thexuong.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity User - Người dùng hệ thống.
 *
 * Lưu ý: schema DB hiện chỉ có cột {@code Users.role NVARCHAR(20) DEFAULT 'CUSTOMER'}
 * (giá trị: CUSTOMER / ADMIN / BOTH). KHÔNG còn bảng Roles / RoleGroups / user_roles
 * / user_role_groups / role_group_roles — phân quyền đơn giản hóa về 1 field String.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"password"})
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

    /**
     * Phân quyền đơn giản: CUSTOMER / ADMIN / BOTH.
     * Bảng Users trong DB đã có sẵn cột {@code role NVARCHAR(20) DEFAULT 'CUSTOMER'}.
     */
    @Column(name = "role", columnDefinition = "NVARCHAR(20)")
    @Builder.Default
    private String role = "CUSTOMER";

    @Column(name = "phone_number")
    private String phoneNumber;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @Builder.Default
    private java.util.List<UserAddress> addresses = new java.util.ArrayList<>();

    /**
     * Trạng thái hoạt động: true = Active, false = Bị khóa.
     * Khi false, Spring Security sẽ từ chối đăng nhập (UserDetails.isEnabled() = false).
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

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
