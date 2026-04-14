package com.example.thexuong.service;

import com.example.thexuong.entity.Role;
import com.example.thexuong.entity.RoleGroup;
import com.example.thexuong.entity.User;
import com.example.thexuong.exception.SelfDeactivationException;
import com.example.thexuong.exception.UserNotFoundException;
import com.example.thexuong.repository.RoleGroupRepository;
import com.example.thexuong.repository.RoleRepository;
import com.example.thexuong.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final RoleGroupRepository roleGroupRepository;

    // ==================== QUERY ====================

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * Tìm User theo ID — ném UserNotFoundException nếu không tìm thấy.
     * Dùng thay cho findById().orElseThrow() rải rác ở Controller.
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    // ==================== PROFILE ====================

    @Transactional
    public void updateProfile(String currentEmail, String fullName, String phoneNumber,
                              String address, String newPassword) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new UserNotFoundException(currentEmail));

        user.setFullName(fullName);
        user.setAddress(address);
        user.setPhoneNumber(phoneNumber);

        // FIX BUG: điều kiện cũ là isEmpty() (không đặt pass) → phải là !isBlank() (có nhập pass mới encode)
        if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }
        userRepository.save(user);
    }

    // ==================== ACTIVE STATUS ====================

    /**
     * Toggle trạng thái active của User (bật ↔ tắt).
     *
     * @param targetUserId  ID của user cần toggle
     * @param currentUserId ID của người đang đăng nhập (lấy từ SecurityContext)
     * @throws SelfDeactivationException nếu Admin cố tắt chính mình
     * @throws UserNotFoundException     nếu không tìm thấy target user
     */
    @Transactional
    public void toggleActive(Long targetUserId, Long currentUserId) {
        // Chặn tự khóa — so sánh Long bằng .equals() để tránh bug auto-unboxing với giá trị lớn
        if (targetUserId.equals(currentUserId)) {
            throw new SelfDeactivationException();
        }

        User target = getUserById(targetUserId);
        // Đảo trạng thái: true → false và ngược lại
        target.setActive(!Boolean.TRUE.equals(target.getActive()));
        userRepository.save(target);
    }

    // ==================== ROLE GROUP ====================

    /**
     * Gán chức danh (RoleGroup) cho User.
     * Nếu roleGroupId = null → gỡ chức danh (set null).
     */
    @Transactional
    public void assignRoleGroup(Long userId, Long roleGroupId) {
        User user = getUserById(userId);

        if (roleGroupId == null) {
            user.setRoleGroup(null);
        } else {
            RoleGroup rg = roleGroupRepository.findById(roleGroupId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chức danh với ID: " + roleGroupId));
            user.setRoleGroup(rg);
        }
        userRepository.save(user);
    }

    // ==================== INDIVIDUAL ROLES ====================

    /**
     * Thêm 1 Role riêng cho User (ngoài Role kế thừa từ RoleGroup).
     */
    @Transactional
    public void addRole(Long userId, Long roleId) {
        User user = getUserById(userId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy role với ID: " + roleId));
        user.getRoles().add(role);
        userRepository.save(user);
    }

    /**
     * Gỡ 1 Role riêng khỏi User.
     */
    @Transactional
    public void removeRole(Long userId, Long roleId) {
        User user = getUserById(userId);
        user.getRoles().removeIf(r -> r.getId().equals(roleId));
        userRepository.save(user);
    }

    /**
     * Đặt lại toàn bộ Roles riêng của User (dùng khi Admin chọn multi-checkbox).
     */
    @Transactional
    public void setRoles(Long userId, Set<Long> roleIds) {
        User user = getUserById(userId);
        Set<Role> newRoles = new HashSet<>(roleRepository.findAllById(roleIds));
        user.setRoles(newRoles);
        userRepository.save(user);
    }

    // ==================== DELETE ====================

    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    // ==================== ADMIN: CREATE USER ====================

    /**
     * Tạo User mới từ form Admin.
     * Tự động gán RoleGroup mặc định là "Khách hàng" nếu không chỉ định.
     * Tự động gán Role "USER" vào user.roles.
     */
    @Transactional
    public User createUser(String email, String username, String fullName,
                           String rawPassword, String provider, Long roleGroupId) {
        User.UserBuilder builder = User.builder()
                .email(email)
                .username(username != null && !username.isBlank() ? username : email)
                .fullName(fullName)
                .provider(provider != null ? provider : "LOCAL")
                .active(true);

        // Mã hóa password nếu là LOCAL, Google user không cần
        if ("LOCAL".equals(provider) && rawPassword != null && !rawPassword.isBlank()) {
            builder.password(passwordEncoder.encode(rawPassword));
        } else {
            builder.password("");
        }

        User user = builder.build();

        // Gán RoleGroup — ưu tiên roleGroupId được truyền vào, fallback về "Khách hàng"
        RoleGroup rg = resolveRoleGroup(roleGroupId);
        user.setRoleGroup(rg);

        // Gán Role mặc định từ bảng Roles (tên "USER")
        roleRepository.findByName("USER").ifPresent(userRole -> user.getRoles().add(userRole));

        return userRepository.save(user);
    }

    /**
     * Resolve RoleGroup: nếu có ID thì dùng, không thì lấy "Khách hàng" làm mặc định.
     */
    private RoleGroup resolveRoleGroup(Long roleGroupId) {
        if (roleGroupId != null) {
            return roleGroupRepository.findById(roleGroupId).orElse(getDefaultRoleGroup());
        }
        return getDefaultRoleGroup();
    }

    /**
     * Lấy RoleGroup mặc định tên "Khách hàng".
     * Nếu chưa có trong DB thì tự tạo mới (safe fallback).
     */
    private RoleGroup getDefaultRoleGroup() {
        return roleGroupRepository.findByName("Khách hàng")
                .orElseGet(() -> {
                    // Tự tạo nếu chưa seed — tránh lỗi khi DB mới setup
                    RoleGroup defaultGroup = RoleGroup.builder()
                            .name("Khách hàng")
                            .description("Chức danh mặc định cho khách hàng thông thường")
                            .build();
                    return roleGroupRepository.save(defaultGroup);
                });
    }
}
