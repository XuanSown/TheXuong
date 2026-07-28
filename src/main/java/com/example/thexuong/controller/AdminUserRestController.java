package com.example.thexuong.controller.api;

import com.example.thexuong.dto.ApiResponse;
import com.example.thexuong.dto.UserStatusDto;
import com.example.thexuong.entity.User;
import com.example.thexuong.repository.UserRepository;
import com.example.thexuong.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller quản lý người dùng.
 * Prefix: /api/admin/users
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'BOTH')")
public class AdminUserRestController {

private final UserService userService;
private final UserRepository userRepository;

private static final java.util.List<String> VALID_ROLES =
java.util.Arrays.asList("CUSTOMER", "ADMIN", "BOTH");

// ==================== GET: Danh sách Users ====================

/**
 * GET /api/v1/admin/users
 * Trả về danh sách tất cả users dưới dạng JSON.
 */
@GetMapping
public ResponseEntity<ApiResponse<List<UserStatusDto>>> getAllUsers() {
List<UserStatusDto> users = userRepository.findAllByOrderByIdAsc()
.stream()
.map(UserStatusDto::from)
.collect(Collectors.toList());

return ResponseEntity.ok(ApiResponse.ok(
"Lay danh sach nguoi dung thanh cong.",
users
));
}

// ==================== PATCH: Toggle Active ====================

/**
 * PATCH /api/v1/admin/users/{id}/toggle-active
 *
 * Bat hoặc tat trang thai active cua User.
 * Tra ve trang thai moi sau khi toggle.
 *
 * Thanh cong -> 200 OK + UserStatusDto
 * Tu khoa -> 400 Bad Request + message (bat boi GlobalExceptionHandler)
 * Khong ton tai -> 404 Not Found (bat boi GlobalExceptionHandler)
 */
@PatchMapping("/{id}/toggle-active")
public ResponseEntity<ApiResponse<UserStatusDto>> toggleActive(@PathVariable Long id) {
Long currentUserId = getCurrentUserId();

// Service se nem SelfDeactivationException neu id == currentUserId
// GlobalExceptionHandler bat va tra 400 Bad Request tu dong
userService.toggleActive(id, currentUserId);

// Load lai user sau khi toggle de tra trang thai moi
User updated = userService.getUserById(id);
String statusText = Boolean.TRUE.equals(updated.getActive()) ? "mo khoa" : "khoa";

return ResponseEntity.ok(ApiResponse.ok(
"Da " + statusText + " tai khoan thanh cong.",
UserStatusDto.from(updated)
));
}

// ==================== DELETE: Xóa User ====================

/**
 * DELETE /api/v1/admin/users/{id}
 * Xóa vĩnh viễn người dùng.
 */
 @DeleteMapping("/{id}")
 public ResponseEntity<ApiResponse<Map<String, Object>>> deleteUser(@PathVariable Long id, Authentication authentication) {
 try {
 if (authentication == null || !authentication.isAuthenticated()) {
 return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
 .body(ApiResponse.error("Chua dang nhap"));
 }
 Long currentUserId = getCurrentUserId();
 if (currentUserId == null) {
 return ResponseEntity.status(HttpStatus.NOT_FOUND)
 .body(ApiResponse.error("Khong tim thay nguoi dung"));
 }
 // Ngăn tự xóa tài khoản
 if (currentUserId.equals(id)) {
 return ResponseEntity.badRequest().body(ApiResponse.error("Khong the xoa tai khoan cua chinh minh"));
 }
 userService.deleteUser(id);
 return ResponseEntity.ok(ApiResponse.ok(
 "Xoa nguoi dung thanh cong",
 Map.of("message", "Xoa nguoi dung thanh cong")
 ));
 } catch (Exception e) {
 log.error("Error deleting user {}: {}", id, e.getMessage(), e);
 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
 .body(ApiResponse.error("Da xay ra loi khi xoa nguoi dung."));
 }
 }

// ==================== PATCH: Cập nhật User ====================

/**
 * PATCH /api/v1/admin/users/{id}
 * Cap nhat thong tin user (fullName, phoneNumber, role)
 */
 @PatchMapping("/{id}")
 public ResponseEntity<ApiResponse<Map<String, Object>>> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication authentication) {
 try {
 if (authentication == null || !authentication.isAuthenticated()) {
 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Chua dang nhap"));
 }
 Long currentUserId = getCurrentUserId();
 if (currentUserId == null) {
 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Khong tim thay nguoi dung"));
 }

 String fullName = body.get("fullName") != null ? body.get("fullName").toString() : null;
 String phoneNumber = body.get("phoneNumber") != null ? body.get("phoneNumber").toString() : null;
 String role = body.get("role") != null ? body.get("role").toString() : null;
 String password = body.get("password") != null ? body.get("password").toString() : null;
 Boolean active = body.get("active") != null ? Boolean.valueOf(body.get("active").toString()) : null;

 if (role != null && !role.isBlank()) {
 String normalizedRole = role.trim().toUpperCase();
 if (!VALID_ROLES.contains(normalizedRole)) {
 return ResponseEntity.badRequest().body(ApiResponse.error("Role khong hop le. Chi cho phep: " + VALID_ROLES));
 }
 userService.setRole(id, normalizedRole);
 }

 if (fullName != null || phoneNumber != null || password != null) {
  userService.updateProfile(id, fullName, phoneNumber, password);
 }

 if (active != null) {
 User u = userService.getUserById(id);
 if (!u.getActive().equals(active)) {
 userService.toggleActive(id, currentUserId);
 }
 }

 User updated = userService.getUserById(id);
 return ResponseEntity.ok(ApiResponse.ok(
 "Cap nhat nguoi dung thanh cong",
 Map.of(
 "message", "Cap nhat nguoi dung thanh cong",
 "id", updated.getId(),
 "email", updated.getEmail(),
 "fullName", updated.getFullName(),
 "role", updated.getRole(),
 "active", updated.getActive(),
 "provider", updated.getProvider()
 )
 ));
 } catch (Exception e) {
 log.error("Error updating user {}: {}", id, e.getMessage(), e);
 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("Da xay ra loi khi cap nhat nguoi dung."));
 }
 }

// ==================== POST: Tạo User ====================

/**
 * POST /api/v1/admin/users
 * Tao user moi tu form admin.
 */
 @PostMapping
 public ResponseEntity<ApiResponse<Map<String, Object>>> createUser(@RequestBody Map<String, Object> body) {
 try {
 String email = body.get("email") != null ? body.get("email").toString() : null;
 String username = body.get("username") != null ? body.get("username").toString() : null;
 String fullName = body.get("fullName") != null ? body.get("fullName").toString() : null;
 String password = body.get("password") != null ? body.get("password").toString() : null;
 String role = body.get("role") != null ? body.get("role").toString() : "CUSTOMER";

 if (email == null || email.isBlank()) {
 return ResponseEntity.badRequest().body(ApiResponse.error("Email khong duoc de trong"));
 }
 if (password == null || password.isBlank()) {
 return ResponseEntity.badRequest().body(ApiResponse.error("Mat khau khong duoc de trong"));
 }

 User created = userService.createUser(email, username, fullName, password, "LOCAL", role);
 return ResponseEntity.ok(ApiResponse.ok(
 "Tao nguoi dung thanh cong",
 Map.of(
 "message", "Tao nguoi dung thanh cong",
 "id", created.getId(),
 "email", created.getEmail(),
 "fullName", created.getFullName(),
 "role", created.getRole(),
 "active", created.getActive(),
 "provider", created.getProvider()
 )
 ));
 } catch (Exception e) {
 log.error("Error creating user: {}", e.getMessage(), e);
 return ResponseEntity.badRequest().body(ApiResponse.error("Da xay ra loi khi tao nguoi dung."));
 }
 }

// ==================== Helper ====================

/** Lay ID cua nguoi dang dang nhap tu SecurityContext. */
private Long getCurrentUserId() {
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
return null;
}
String username = auth.getName(); // Spring Security luu email lam principal
return userRepository.findByEmail(username)
.or(() -> userRepository.findByUsername(username))
.map(User::getId)
.orElse(null);
}
}
