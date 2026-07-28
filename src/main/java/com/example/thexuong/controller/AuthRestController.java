package com.example.thexuong.controller;

import com.example.thexuong.dto.UserResponse;
import com.example.thexuong.dto.auth.ForgotPasswordRequest;
import com.example.thexuong.dto.auth.LoginRequest;
import com.example.thexuong.dto.auth.RegisterRequest;
import com.example.thexuong.dto.auth.UpdateProfileRequest;
import com.example.thexuong.entity.User;
import com.example.thexuong.service.PasswordResetService;
import com.example.thexuong.service.UserService;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API for Authentication (Vue frontend consumption).
 * All endpoints are session-based (no JWT).
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final PasswordResetService passwordResetService;
    private final com.example.thexuong.security.JwtService jwtService;
    private final com.example.thexuong.security.JwtCookieService jwtCookieService;
    private final com.example.thexuong.security.TokenBlacklist tokenBlacklist;
    private final org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    /**
     * POST /api/auth/login
     * Body: { email, password }
     * Returns: { user: UserResponse }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest,
                                   HttpServletResponse httpResponse) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw e; // GlobalExceptionHandler returns 401
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        jwtCookieService.setAuthCookies(httpResponse, accessToken, refreshToken);

        User user = userService.getUserByEmailWithAddresses(request.getEmail());
        UserResponse userResponse = toUserResponse(user);

        Map<String, Object> data = new HashMap<>();
        data.put("user", userResponse);
        data.put("message", "Đăng nhập thành công");

        return ResponseEntity.ok(data);
    }

    /**
     * POST /api/v1/auth/logout
     * Hủy session (JSESSIONID) + clear SecurityContext + xóa cookie.
     * SPA gọi endpoint này (SecurityConfig's /logout chỉ fallback cho non-API).
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = jwtCookieService.readCookie(request, "access_token");
        if (accessToken != null) {
            try {
                var claims = jwtService.extractClaims(accessToken);
                tokenBlacklist.blacklist(claims.getId(), claims.getExpiration().toInstant());
            } catch (Exception ignored) {}
        }
        String refreshToken = jwtCookieService.readCookie(request, "refresh_token");
        if (refreshToken != null) {
            try {
                var claims = jwtService.extractClaims(refreshToken);
                tokenBlacklist.blacklist(claims.getId(), claims.getExpiration().toInstant());
            } catch (Exception ignored) {}
        }
        SecurityContextHolder.clearContext();
        jwtCookieService.clearAuthCookies(response);
        return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtCookieService.readCookie(request, "refresh_token");
        if (refreshToken == null || !jwtService.isValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Refresh token không hợp lệ"));
        }
        String email = jwtService.extractUsername(refreshToken);
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            String newAccess = jwtService.generateAccessToken(userDetails);
            String newRefresh = jwtService.generateRefreshToken(userDetails);
            jwtCookieService.setAuthCookies(response, newAccess, newRefresh);

            User user = userService.getUserByEmailWithAddresses(email);
            return ResponseEntity.ok(Map.of("user", toUserResponse(user)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Không thể refresh token"));
        }
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
        User user = userService.getUserByEmailWithAddresses(email);
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
                    null // role = USER by default
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Đăng ký thành công. Vui lòng đăng nhập."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Đã xảy ra lỗi: " + e.getMessage()));
        }
    }

    /**
     * POST /api/auth/forgot-password
     * Body: { email }
     * Returns: message (always success to prevent email enumeration)
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            passwordResetService.createPasswordResetToken(request.getEmail());
        } catch (Exception e) {
            log.warn("Forgot password failed for email {}: {}", request.getEmail(), e.getMessage());
        }
        return ResponseEntity.ok(Map.of(
                "message", "Nếu email tồn tại, hướng dẫn đặt lại mật khẩu sẽ được gửi."
        ));
    }

    /**
     * POST /api/auth/reset-password
     * Body: { token, password, confirmPassword }
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String password = body.get("password");
        String confirmPassword = body.get("confirmPassword");

        if (token == null || password == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Thiếu thông tin"));
        }

        if (!password.equals(confirmPassword)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Xác nhận mật khẩu không khớp"));
        }

        try {
            passwordResetService.resetPassword(token, password);
            return ResponseEntity.ok(Map.of("message", "Đặt lại mật khẩu thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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
                null // no password change in this endpoint
        );

        return ResponseEntity.ok(Map.of(
                "message", "Cập nhật thông tin thành công",
                "user", toUserResponse(userService.getUserByEmailWithAddresses(email))
        ));
    }

    /**
     * PUT /api/auth/password
     * Change password - Only for LOCAL provider accounts
     */
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(
            Authentication authentication,
            @RequestBody Map<String, String> body) {
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
                    .body(Map.of("error", "Tài khoản " + user.getProvider() + " không thể đổi mật khẩu. Vui lòng sử dụng tài khoản local."));
        }

        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");

        if (currentPassword == null || newPassword == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Thiếu thông tin mật khẩu"));
        }

        // Verify current password and update
        try {
            userService.changePassword(email, currentPassword, newPassword);
            return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Đã xảy ra lỗi: " + e.getMessage()));
        }
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .addresses(user.getAddresses() == null ? java.util.List.of() :
                    user.getAddresses().stream().map(a -> com.example.thexuong.dto.address.AddressResponse.builder()
                        .id(a.getId()).label(a.getLabel())
                        .recipientName(a.getRecipientName()).recipientPhone(a.getRecipientPhone())
                        .provinceCode(a.getProvinceCode()).districtCode(a.getDistrictCode()).wardCode(a.getWardCode())
                        .streetDetail(a.getStreetDetail())
                        .latitude(a.getLatitude()).longitude(a.getLongitude())
                        .isDefault(a.getIsDefault())
                        .build()).toList())
                .role(user.getRole())
                .provider(user.getProvider())
                .active(user.getActive())
                .build();
    }
}
