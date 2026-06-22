package com.example.thexuong.controller.api;

import com.example.thexuong.entity.User;
import com.example.thexuong.entity.Role;
import com.example.thexuong.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final UserService userService;

    // DTOs
    public record LoginRequest(
            String email,
            String password
    ) {}

    public record RegisterRequest(
            String fullName,
            String email,
            String password,
            String confirmPassword
    ) {}

    public record UserResponse(
            Long id,
            String username,
            String email,
            String fullName,
            String phone,
            String address,
            String[] roles,
            boolean enabled
    ) {}

    public record MessageResponse(
            String message
    ) {}

    // GET /api/v1/auth/user - Get current user
    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).build();
        }

        String email = auth.getName();
        User user = userService.getUserByEmail(email);

        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        UserResponse response = new UserResponse(
                user.getId(),
                user.getUsername() != null ? user.getUsername() : user.getEmail(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getAddress(),
                user.getRoles().stream()
                        .map(Role::getName)
                        .toArray(String[]::new),
                Boolean.TRUE.equals(user.getActive())
        );

        return ResponseEntity.ok(response);
    }

    // POST /api/v1/auth/login - Login (session-based)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Spring Security handles authentication via form login
        return ResponseEntity.ok(new MessageResponse("Login successful"));
    }

    // POST /api/v1/auth/logout - Logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(new MessageResponse("Logout successful"));
    }

    // POST /api/v1/auth/register - Register new user
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Passwords do not match"));
        }

        try {
            userService.createUser(
                    request.email(),
                    null,
                    request.fullName(),
                    request.password(),
                    "LOCAL",
                    null
            );
            return ResponseEntity.ok(new MessageResponse("Registration successful"));
        } catch (Exception e) {
            String message = e.getMessage();
            if (message != null && message.toLowerCase().contains("email")) {
                return ResponseEntity.badRequest().body(new MessageResponse("Email already exists"));
            }
            return ResponseEntity.badRequest().body(new MessageResponse("Registration failed: " + message));
        }
    }

    // POST /api/v1/auth/forgot-password - Send reset email
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        // TODO: Implement password reset logic
        return ResponseEntity.ok(new MessageResponse("Password reset email sent if email exists"));
    }
}
