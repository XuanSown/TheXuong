// ponytail: in-memory token store — single-instance only. Move to a DB table when scaling out so reset links survive restarts and work across replicas.
package com.example.thexuong.service;

import com.example.thexuong.entity.User;
import com.example.thexuong.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private static final long TOKEN_TTL_MINUTES = 30;

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend.url}")
    private String frontendBaseUrl;

    private final Map<String, TokenEntry> tokens = new ConcurrentHashMap<>();

    private record TokenEntry(Long userId, LocalDateTime expiresAt) {}

    @Transactional
    public void createPasswordResetToken(String email) {
        if (email == null || email.isBlank()) return;
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            log.info("Password reset requested for non-existent email: {}", email);
            return;
        }
        String token = UUID.randomUUID().toString();
        tokens.put(token, new TokenEntry(user.getId(), LocalDateTime.now().plusMinutes(TOKEN_TTL_MINUTES)));
        String resetUrl = frontendBaseUrl + "/reset-password?token=" + token;
        try {
            emailService.sendPasswordResetLink(user.getEmail(), resetUrl);
        } catch (Exception e) {
            log.error("Failed to send password reset email: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        if (token == null) throw new RuntimeException("Token khong hop le.");
        TokenEntry entry = tokens.remove(token);
        if (entry == null) throw new RuntimeException("Token khong hop le hoac da duoc su dung.");
        if (entry.expiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token da het han.");
        }
        User user = userRepository.findById(entry.userId())
                .orElseThrow(() -> new RuntimeException("Nguoi dung khong ton tai."));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password reset successful for user {}", user.getId());
    }
}
