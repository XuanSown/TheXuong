package com.example.thexuong.controller;

import com.example.thexuong.dto.UserResponse;
import com.example.thexuong.dto.auth.ChangePasswordRequest;
import com.example.thexuong.dto.auth.ForgotPasswordRequest;
import com.example.thexuong.dto.auth.LoginRequest;
import com.example.thexuong.dto.auth.RegisterRequest;
import com.example.thexuong.dto.auth.UpdateProfileRequest;
import com.example.thexuong.entity.User;
import com.example.thexuong.filter.LoginRateLimitFilter;
import com.example.thexuong.service.PasswordResetService;
import com.example.thexuong.service.UserService;
import com.example.thexuong.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API for Authentication (Vue frontend consumption).
 * Stateless: uses JWT Bearer tokens (see JwtAuthenticationFilter).
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final PasswordResetService passwordResetService;
    private final LoginRateLimitFilter loginRateLimitFilter;
    private final JwtService jwtService;

    /**
     * POST /api/auth/login
     * Body: { email, password }
     * Returns: { accessToken, user: UserResponse }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // Stateless: tạo JWT thay vì lưu session
            org.springframework.security.core.userdetails.UserDetails userDetails =
                    (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
            String accessToken = jwtService.generateToken(userDetails);

            // Lấy email trực tiếp từ Principal (đã được load trong authenticate() ở trên)
            String email = authentication.getName();
            User user = userService.getUserByEmail(email);
            UserResponse userResponse = toUserResponse(user);

            Map<String, Object> data = new HashMap<>();
            data.put("accessToken", accessToken);
            data.put("user", userResponse);
            data.put("message", "Đăng nhập thành công");

            return ResponseEntity.ok(data);
        } catch (org.springframework.security.core.AuthenticationException e) {
            // Ghi nhận lần login thất bại cho rate limiter
            String clientIp = httpRequest.getRemoteAddr();
            loginRateLimitFilter.recordFailedAttempt(clientIp);
            log.error("Login failed for {} from {}: {}", request.getEmail(), clientIp, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Email hoặc mật khẩu không chính xác"));
        }
    }

    /**
     * POST /api/auth/logout
     * Stateless no-op: client clears the JWT from localStorage.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
    }

    /**
     * GET /api/auth/user
     * Returns current authenticated user
     */
    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Chưa đăng nhập"));
        }

        String email = authentication.getName();
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Không tìm thấy người dùng"));
        }

        return ResponseEntity.ok(Map.of("user", toUserResponse(user)));
    }

    /**
     * POST /api/auth/register
     * Body: { fullName, email, password, confirmPassword }
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // Check if email already exists
            if (userService.getUserByEmail(request.getEmail()) != null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email đã được đăng ký"));
            }

            // Check password confirmation matches
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Xác nhận mật khẩu không khớp"));
            }

            // Create user
            userService.createUser(
                    request.getEmail(),
                    null, // username = null → use email
                    request.getFullName(),
                    request.getPassword(),
                    "LOCAL",
                    null // role = CUSTOMER by default
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Đăng ký thành công. Vui lòng đăng nhập."));
        } catch (Exception e) {
            log.error("Registration failed for email {}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau."));
        }
    }

    /**
     * POST /api/auth/forgot-password
     * Body: { email }
     * Luôn trả về message chung (không tiết lộ email có tồn tại hay không)
     * để ngăn email enumeration attack.
     * Nếu email tồn tại và là tài khoản LOCAL → gửi email reset link.
     * Nếu email không tồn tại hoặc là OAuth user → vẫn trả về message giống nhau.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String email = request.getEmail();
        log.info("[FORGOT_PASSWORD] Request received for email: {}", email);

        try {
            passwordResetService.createPasswordResetToken(email);
            log.info("[FORGOT_PASSWORD] Reset token created and email sent for: {}", email);
        } catch (RuntimeException e) {
            // Không log chi tiết lỗi để tránh information leakage
            // Có thể là: user không tồn tại, hoặc là OAuth user
            log.warn("[FORGOT_PASSWORD] Cannot process reset for {}: {}", email, e.getMessage());
        }

        // Luôn trả về cùng một message — ngăn attacker kiểm tra email nào tồn tại
        return ResponseEntity.ok(Map.of(
                "message", "Nếu email tồn tại trong hệ thống và là tài khoản local, " +
                        "hướng dẫn đặt lại mật khẩu sẽ được gửi đến email của bạn."
        ));
    }

    /**
     * POST /api/auth/reset-password
     * Body: { token, newPassword }
     * Đặt lại mật khẩu bằng token nhận được qua email.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");

        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Token không hợp lệ"));
        }
        if (newPassword == null || newPassword.length() < 8) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Mật khẩu mới phải có ít nhất 8 ký tự"));
        }

        try {
            passwordResetService.resetPassword(token, newPassword);
            log.info("[RESET_PASSWORD] Password reset successful for token: {}...", token.substring(0, 8));
            return ResponseEntity.ok(Map.of(
                    "message", "Đặt lại mật khẩu thành công. Vui lòng đăng nhập bằng mật khẩu mới."
            ));
        } catch (RuntimeException e) {
            log.warn("[RESET_PASSWORD] Failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/auth/profile
     * Update current user profile
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Chưa đăng nhập"));
        }

        String email = authentication.getName();
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Không tìm thấy người dùng"));
        }

        userService.updateProfile(
                email,
                request.getFullName(),
                request.getPhoneNumber(),
                request.getAddress(),
                null // no password change in this endpoint
        );

        return ResponseEntity.ok(Map.of(
                "message", "Cập nhật thông tin thành công",
                "user", toUserResponse(userService.getUserByEmail(email))
        ));
    }

    /**
     * PUT /api/auth/password
     * Change password - Only for LOCAL provider accounts
     */
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Chưa đăng nhập"));
        }

        String email = authentication.getName();
        User user = userService.getUserByEmail(email);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Không tìm thấy người dùng"));
        }

        // Check if user is OAuth (Google) - cannot change password
        if (user.getProvider() != null && !user.getProvider().equals("LOCAL")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Tài khoản " + user.getProvider() +
                            " không thể đổi mật khẩu. Vui lòng sử dụng tài khoản local."));
        }

        // Verify password confirmation matches
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Xác nhận mật khẩu không khớp"));
        }

        try {
            userService.changePassword(email, request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công"));
        } catch (IllegalArgumentException e) {
            log.warn("Password change failed for user {}: {}", email, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error changing password for user {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau."));
        }
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .role(user.getRole())
                .provider(user.getProvider())
                .active(user.getActive())
                .build();
    }
}
