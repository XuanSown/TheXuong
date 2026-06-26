package com.example.thexuong.service;

import com.example.thexuong.entity.User;
import com.example.thexuong.exception.SelfDeactivationException;
import com.example.thexuong.exception.UserNotFoundException;
import com.example.thexuong.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ==================== QUERY ====================

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * Tìm User theo ID — ném UserNotFoundException nếu không tìm thấy.
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

    /**
     * Change password for LOCAL accounts only
     * Throws IllegalArgumentException if:
     * - User is OAuth (non-LOCAL provider)
     * - Current password is incorrect
     */
    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        // Check if user is OAuth - cannot change password
        if (user.getProvider() != null && !"LOCAL".equals(user.getProvider())) {
            throw new IllegalArgumentException(
                "Tài khoản " + user.getProvider() + " không thể đổi mật khẩu. Vui lòng sử dụng tài khoản local."
            );
        }

        // Verify current password (handle null for OAuth users who might have empty password)
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Tài khoản này không có mật khẩu để xác thực.");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng.");
        }

        // Encode and set new password
        user.setPassword(passwordEncoder.encode(newPassword));
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

    // ==================== ROLE (đơn giản) ====================

    /**
     * Cập nhật role (String) cho User. Không dùng Set&lt;Role&gt; / Set&lt;RoleGroup&gt; nữa.
     * Nếu {@code role} null/blank → giữ nguyên (không đổi).
     */
    @Transactional
    public void setRole(Long userId, String role) {
        if (role == null || role.isBlank()) return;
        User user = getUserById(userId);
        user.setRole(role.toUpperCase());
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
     *
     * @param email         Email (bắt buộc, unique)
     * @param username      Username (có thể null → tự dùng email)
     * @param fullName      Họ tên
     * @param rawPassword   Mật khẩu thô (sẽ được BCrypt encode)
     * @param provider      "LOCAL" hoặc "GOOGLE"
     * @param role          Role mong muốn (USER / ADMIN / BOTH). Null/blank → mặc định "USER".
     */
    @Transactional
    public User createUser(String email, String username, String fullName,
                           String rawPassword, String provider, String role) {
        String finalRole = (role == null || role.isBlank()) ? "USER" : role.toUpperCase();

        User.UserBuilder builder = User.builder()
                .email(email)
                .username(username != null && !username.isBlank() ? username : email)
                .fullName(fullName)
                .provider(provider != null ? provider : "LOCAL")
                .role(finalRole)
                .active(true);

        // Mã hóa password nếu là LOCAL, Google user không cần
        if ("LOCAL".equals(provider) && rawPassword != null && !rawPassword.isBlank()) {
            builder.password(passwordEncoder.encode(rawPassword));
        } else {
            builder.password("");
        }

        return userRepository.save(builder.build());
    }
}
