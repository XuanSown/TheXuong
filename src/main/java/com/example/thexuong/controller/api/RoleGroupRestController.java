package com.example.thexuong.controller.api;

import com.example.thexuong.dto.ApiResponse;
import com.example.thexuong.dto.RoleGroupRequest;
import com.example.thexuong.dto.RoleGroupResponse;
import com.example.thexuong.entity.RoleGroup;
import com.example.thexuong.repository.RoleRepository;
import com.example.thexuong.service.RoleGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller quản lý RoleGroup (Chức danh).
 * Prefix: /api/admin/role-groups
 *
 * Tất cả endpoint đều yêu cầu quyền ADMIN hoặc BOTH.
 */
@RestController
@RequestMapping("/api/admin/role-groups")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'BOTH')") // Bảo vệ toàn bộ Controller
public class RoleGroupRestController {

    private final RoleGroupService roleGroupService;
    private final RoleRepository roleRepository;

    // ==================== GET: Danh sách ====================

    /**
     * GET /api/admin/role-groups
     * Lấy tất cả RoleGroups.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleGroupResponse>>> getAll() {
        List<RoleGroupResponse> groups = roleGroupService.getAllRoleGroups()
                .stream()
                .map(RoleGroupResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(
                "Lấy danh sách chức danh thành công.",
                groups
        ));
    }

    // ==================== GET: Chi tiết ====================

    /**
     * GET /api/admin/role-groups/{id}
     * Lấy chi tiết 1 RoleGroup kèm danh sách Roles.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleGroupResponse>> getById(@PathVariable Long id) {
        RoleGroup rg = roleGroupService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(
                "Lấy thông tin chức danh thành công.",
                RoleGroupResponse.from(rg)
        ));
    }

    // ==================== POST: Tạo mới ====================

    /**
     * POST /api/admin/role-groups
     * Body: { "name": "Quản lý kho", "description": "...", "roleIds": [1, 2] }
     *
     * Trả về 201 Created + RoleGroup vừa tạo.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RoleGroupResponse>> create(@RequestBody RoleGroupRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("Tên chức danh không được để trống."));
        }

        // Build entity từ request
        RoleGroup rg = RoleGroup.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .build();

        // Gán roles nếu có truyền roleIds
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            rg.setRoles(new HashSet<>(roleRepository.findAllById(request.getRoleIds())));
        }

        RoleGroup saved = roleGroupService.save(rg);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tạo chức danh thành công.", RoleGroupResponse.from(saved)));
    }

    // ==================== PUT: Cập nhật ====================

    /**
     * PUT /api/admin/role-groups/{id}
     * Body: { "name": "Tên mới", "description": "...", "roleIds": [1, 3] }
     *
     * roleIds = null → không thay đổi roles hiện tại.
     * roleIds = []   → gỡ toàn bộ roles khỏi group.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleGroupResponse>> update(
            @PathVariable Long id,
            @RequestBody RoleGroupRequest request) {

        RoleGroup existing = roleGroupService.findById(id);

        // Chỉ cập nhật field nếu không null
        if (request.getName() != null && !request.getName().isBlank()) {
            existing.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }

        // roleIds != null → thay toàn bộ roles (null = giữ nguyên)
        if (request.getRoleIds() != null) {
            existing.setRoles(new HashSet<>(roleRepository.findAllById(request.getRoleIds())));
        }

        RoleGroup saved = roleGroupService.save(existing);
        return ResponseEntity.ok(ApiResponse.ok(
                "Cập nhật chức danh thành công.",
                RoleGroupResponse.from(saved)
        ));
    }

    // ==================== DELETE: Xóa ====================

    /**
     * DELETE /api/admin/role-groups/{id}
     *
     * Thành công → 200 OK
     * Còn User   → 409 Conflict (bắt bởi GlobalExceptionHandler)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        // Service sẽ ném RoleGroupInUseException nếu còn User → GlobalExceptionHandler bắt → 409
        roleGroupService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Đã xóa chức danh thành công."));
    }

    // ==================== POST: Thêm Role vào Group ====================

    /**
     * POST /api/admin/role-groups/{id}/roles/{roleId}
     * Thêm 1 Role vào RoleGroup (gán quyền thêm cho chức danh).
     */
    @PostMapping("/{id}/roles/{roleId}")
    public ResponseEntity<ApiResponse<RoleGroupResponse>> addRole(
            @PathVariable Long id,
            @PathVariable Long roleId) {

        roleGroupService.addRoleToGroup(id, roleId);
        RoleGroup updated = roleGroupService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(
                "Đã thêm quyền vào chức danh.",
                RoleGroupResponse.from(updated)
        ));
    }

    // ==================== DELETE: Gỡ Role khỏi Group ====================

    /**
     * DELETE /api/admin/role-groups/{id}/roles/{roleId}
     * Gỡ 1 Role khỏi RoleGroup (thu hồi quyền của chức danh).
     */
    @DeleteMapping("/{id}/roles/{roleId}")
    public ResponseEntity<ApiResponse<RoleGroupResponse>> removeRole(
            @PathVariable Long id,
            @PathVariable Long roleId) {

        roleGroupService.removeRoleFromGroup(id, roleId);
        RoleGroup updated = roleGroupService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(
                "Đã gỡ quyền khỏi chức danh.",
                RoleGroupResponse.from(updated)
        ));
    }
}
