package com.example.thexuong.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Request DTO khi tạo mới hoặc cập nhật RoleGroup.
 *
 * Ví dụ JSON gửi lên:
 * {
 *   "name": "Quản lý kho",
 *   "description": "Nhân viên quản lý tồn kho",
 *   "roleIds": [1, 2]
 * }
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleGroupRequest {
    private String name;
    private String description;
    /** IDs của các Role gán cho chức danh này. Null = không thay đổi roles hiện tại. */
    private Set<Long> roleIds;
}
