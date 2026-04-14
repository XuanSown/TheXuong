package com.example.thexuong.dto;

import com.example.thexuong.entity.RoleGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Response DTO trả về thông tin RoleGroup đầy đủ, kèm danh sách Roles.
 *
 * Ví dụ JSON trả về:
 * {
 *   "id": 2,
 *   "name": "Quản lý kho",
 *   "description": "...",
 *   "roles": [
 *     { "id": 1, "name": "USER",  "description": "Khách hàng" },
 *     { "id": 2, "name": "ADMIN", "description": "Quản trị viên" }
 *   ]
 * }
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleGroupResponse {

    private Long id;
    private String name;
    private String description;
    private Set<RoleInfo> roles;

    /** Nested DTO cho thông tin Role — tránh expose entity Role trực tiếp. */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleInfo {
        private Long id;
        private String name;
        private String description;
    }

    /** Chuyển từ Entity sang Response DTO. */
    public static RoleGroupResponse from(RoleGroup rg) {
        Set<RoleInfo> roleInfos = rg.getRoles().stream()
                .map(r -> RoleInfo.builder()
                        .id(r.getId())
                        .name(r.getName())
                        .description(r.getDescription())
                        .build())
                .collect(Collectors.toSet());

        return RoleGroupResponse.builder()
                .id(rg.getId())
                .name(rg.getName())
                .description(rg.getDescription())
                .roles(roleInfos)
                .build();
    }
}
