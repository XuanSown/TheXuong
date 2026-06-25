package com.example.thexuong.controller;

import com.example.thexuong.entity.User;
import com.example.thexuong.repository.UserRepository;
import com.example.thexuong.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin REST API for User Management.
 * Base path: /api/v1/admin/users
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserRestController {

    private final UserRepository userRepository;
    private final UserService userService;

    /**
     * GET /api/admin/users
     * List all users (basic info)
     */
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> userList = users.stream()
                .map(u -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", u.getId());
                    map.put("email", u.getEmail());
                    map.put("fullName", u.getFullName());
                    map.put("role", u.getRole());
                    map.put("active", u.getActive());
                    map.put("provider", u.getProvider());
                    map.put("phoneNumber", u.getPhoneNumber());
                    map.put("tierCode", u.getTierCode());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(userList);
    }

    /**
     * PATCH /api/v1/admin/users/{id}/toggle-active
     * Toggle user active status
     */
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<?> toggleUserActive(@PathVariable Long id, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Chưa đăng nhập"));
            }

            String email = authentication.getName();
            com.example.thexuong.entity.User currentUser = userService.getUserByEmail(email);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy người dùng"));
            }

            userService.toggleActive(id, currentUser.getId());
            User user = userService.getUserById(id);

            return ResponseEntity.ok(Map.of(
                    "message", "Cập nhật trạng thái thành công",
                    "active", user.getActive()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
