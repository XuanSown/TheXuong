package com.example.thexuong.controller.api;

import com.example.thexuong.dto.ApiResponse;
import com.example.thexuong.dto.UserStatusDto;
import com.example.thexuong.entity.User;
import com.example.thexuong.repository.UserRepository;
import com.example.thexuong.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller quản lý người dùng.
 * Chạy SONG SONG với UserManagementController (Thymeleaf) — không thay thế.
 * Prefix: /api/admin/users
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'BOTH')") // Bảo vệ toàn bộ Controller
public class UserManagementRestController {

    private final UserService userService;
    private final UserRepository userRepository;

    // ==================== GET: Danh sách Users ====================

    /**
     * GET /api/admin/users
     * Trả về danh sách tất cả users dưới dạng JSON.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserStatusDto>>> getAllUsers() {
        List<UserStatusDto> users = userRepository.findAllByOrderByIdAsc()
                .stream()
                .map(UserStatusDto::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(
                "Lấy danh sách người dùng thành công.",
                users
        ));
    }

    // ==================== PATCH: Toggle Active ====================

    /**
     * PATCH /api/admin/users/{id}/toggle-active
     *
     * Bật hoặc tắt trạng thái active của User.
     * Trả về trạng thái mới sau khi toggle.
     *
     * Thành công → 200 OK + UserStatusDto
     * Tự khóa   → 400 Bad Request + message (bắt bởi GlobalExceptionHandler)
     * Không tồn tại → 404 Not Found (bắt bởi GlobalExceptionHandler)
     */
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<UserStatusDto>> toggleActive(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();

        // Service sẽ ném SelfDeactivationException nếu id == currentUserId
        // GlobalExceptionHandler bắt và trả 400 Bad Request tự động
        userService.toggleActive(id, currentUserId);

        // Load lại user sau khi toggle để trả trạng thái mới
        User updated = userService.getUserById(id);
        String statusText = Boolean.TRUE.equals(updated.getActive()) ? "mở khóa" : "khóa";

        return ResponseEntity.ok(ApiResponse.ok(
                "Đã " + statusText + " tài khoản thành công.",
                UserStatusDto.from(updated)
        ));
    }

    // ==================== Helper ====================

    /** Lấy ID của người đang đăng nhập từ SecurityContext. */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        String username = auth.getName(); // Spring Security lưu email làm principal
        return userRepository.findByEmail(username)
                .or(() -> userRepository.findByUsername(username))
                .map(User::getId)
                .orElse(null);
    }
}
