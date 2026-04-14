package com.example.thexuong.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity RoleGroup - Chức danh (VD: Giám đốc, Quản lý kho, Khách hàng).
 * 1 RoleGroup chứa nhiều Role mặc định.
 * QUAN TRỌNG: Dùng @Getter/@Setter thay @Data để tránh StackOverflowError
 * khi toString() duyệt qua Set<Role> rồi Role lại trỏ ngược về RoleGroup.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"roles"})
@Entity
@Table(name = "RoleGroups")
public class RoleGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true, columnDefinition = "NVARCHAR(100)")
    private String name; // VD: "Giám đốc", "Quản lý kho", "Khách hàng"

    @Column(columnDefinition = "NVARCHAR(255)")
    private String description;

    /**
     * Tập hợp Role mặc định của chức danh này.
     * CascadeType.PERSIST/MERGE: Khi lưu RoleGroup, cập nhật bảng trung gian.
     * KHÔNG CascadeType.REMOVE: Xóa RoleGroup không được xóa Role gốc.
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "role_group_roles",
            joinColumns = @JoinColumn(name = "role_group_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}
