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

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository userRepository;

    /**
     * UserDetailsService: Load User từ DB cho Spring Security.
     *
     * Logic gom authority:
     *   - Lấy roles riêng của User (user.roles)
     *   - Lấy roles kế thừa từ RoleGroup (user.roleGroup.roles)
     *   - Union 2 tập → Set<GrantedAuthority> (tự loại bỏ trùng lặp)
     *
     * Dùng findWithRolesByEmail() có @EntityGraph → 1 query duy nhất, tránh N+1.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            // Ưu tiên tìm bằng Email, fallback sang Username
            // Dùng findWithRoles* để EAGER fetch roles + roleGroup.roles trong 1 query
            com.example.thexuong.entity.User user = userRepository
                    .findWithRolesByEmail(username)
                    .or(() -> userRepository.findWithRolesByUsername(username))
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "Không tìm thấy người dùng với email/username: " + username));

            // Gom quyền = Roles riêng của User ∪ Roles kế thừa từ RoleGroup
            Set<GrantedAuthority> authorities = Stream.concat(
                    // Roles riêng của User
                    user.getRoles().stream(),
                    // Roles từ Chức danh (RoleGroup) — bỏ qua nếu chưa gán chức danh
                    user.getRoleGroup() != null
                            ? user.getRoleGroup().getRoles().stream()
                            : Stream.empty()
            )
            .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role.getName()))
            .collect(Collectors.toSet());

            // Truyền user.active vào Spring Security:
            // enabled=false → Spring từ chối login, redirect về /login?error (DisabledException)
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword() == null ? "" : user.getPassword(),
                    Boolean.TRUE.equals(user.getActive()),  // enabled
                    true,  // accountNonExpired
                    true,  // credentialsNonExpired
                    true,  // accountNonLocked
                    authorities
            );
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
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
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