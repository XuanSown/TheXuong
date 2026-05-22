package com.example.thexuong.service;

import com.example.thexuong.entity.Role;
import com.example.thexuong.entity.RoleGroup;
import com.example.thexuong.exception.RoleGroupInUseException;
import com.example.thexuong.repository.RoleGroupRepository;
import com.example.thexuong.repository.RoleRepository;
import com.example.thexuong.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleGroupService {

    private final RoleGroupRepository roleGroupRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    /** Lấy toàn bộ danh sách Chức danh. */
    public List<RoleGroup> getAllRoleGroups() {
        return roleGroupRepository.findAll();
    }

    /**
     * Tìm RoleGroup theo ID.
     * Ném IllegalArgumentException nếu không tồn tại (tránh tạo class exception riêng chỉ để reuse ở 1 chỗ).
     */
    public RoleGroup findById(Long id) {
        return roleGroupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chức danh với ID: " + id));
    }

    /** Tạo hoặc cập nhật RoleGroup. */
    @Transactional
    public RoleGroup save(RoleGroup roleGroup) {
        return roleGroupRepository.save(roleGroup);
    }

    /**
     * Xóa RoleGroup theo ID.
     * Chặn xóa nếu vẫn còn User thuộc chức danh này (bắt DataIntegrityViolationException từ DB).
     */
    @Transactional
    public void deleteById(Long id) {
        RoleGroup rg = findById(id);

        // Kiểm tra trước ở tầng Service để báo lỗi thân thiện hơn
        long userCount = userRepository.countByRoleGroups_Id(id);
        if (userCount > 0) {
            throw new RoleGroupInUseException(rg.getName());
        }

        try {
            roleGroupRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            // Fallback: DB constraint bắt được nếu check bên trên bị race condition
            throw new RoleGroupInUseException(rg.getName());
        }
    }

    /**
     * Thêm Role vào RoleGroup (gán quyền cho chức danh).
     * VD: Thêm quyền PRODUCT_MANAGER vào chức danh "Quản lý kho".
     */
    @Transactional
    public void addRoleToGroup(Long roleGroupId, Long roleId) {
        RoleGroup rg = findById(roleGroupId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy role với ID: " + roleId));
        rg.getRoles().add(role);
        roleGroupRepository.save(rg);
    }

    /**
     * Gỡ Role khỏi RoleGroup (thu hồi quyền của chức danh).
     */
    @Transactional
    public void removeRoleFromGroup(Long roleGroupId, Long roleId) {
        RoleGroup rg = findById(roleGroupId);
        rg.getRoles().removeIf(r -> r.getId().equals(roleId));
        roleGroupRepository.save(rg);
    }
}
