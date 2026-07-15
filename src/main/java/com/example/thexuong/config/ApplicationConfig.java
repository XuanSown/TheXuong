package com.example.thexuong.config;

import com.example.thexuong.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository userRepository;

    /**
     * UserDetailsService: Load User từ DB cho Spring Security.
     *
     * Phân quyền đơn giản hóa: User chỉ có 1 field {@code role} (CUSTOMER / ADMIN / BOTH).
     * → Authority = {@code SimpleGrantedAuthority(user.role)} (1 authority duy nhất).
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            // Ưu tiên tìm bằng Email, fallback sang Username
            com.example.thexuong.entity.User user = userRepository
                    .findWithRolesByEmail(username)
                    .or(() -> userRepository.findWithRolesByUsername(username))
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "Không tìm thấy người dùng với email/username: " + username));

            // Authority đơn từ cột user.role (CUSTOMER / ADMIN / BOTH).
            // Nếu role null/blank (DB cũ chưa có) → fallback "CUSTOMER".
            String role = (user.getRole() == null || user.getRole().isBlank()) ? "CUSTOMER" : user.getRole();
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

            // Truyền user.active vào Spring Security:
            // enabled=false → Spring từ chối login, redirect về /login?error (DisabledException)
            return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                    .password(user.getPassword() == null ? "" : user.getPassword())
                    .disabled(!Boolean.TRUE.equals(user.getActive()))
                    .accountExpired(false)
                    .credentialsExpired(false)
                    .accountLocked(false)
                    .authorities(authorities)
                    .build();
        };
    }

    /**
     * PasswordEncoder: BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationProvider: Kết hợp UserDetailsService + PasswordEncoder
     * Lưu ý: DaoAuthenticationProvider mặc định gọi loadUserByUsername() 1 lần duy nhất.
     * Nếu vẫn thấy double query, nguyên nhân từ JPA Session flush hoặc Spring Session filter.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        // Tắt hide exception để dễ debug lỗi đăng nhập
        authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;
    }

    /**
     * AuthenticationManager: Cần khi login thủ công (VD: sau khi đăng ký)
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
