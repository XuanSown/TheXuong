package com.example.thexuong.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.AuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.thexuong.entity.User;
import com.example.thexuong.repository.UserRepository;
import com.example.thexuong.security.OAuth2SuccessHandler;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Bật @PreAuthorize/@PostAuthorize cho Method Security
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final AuthenticationProvider authenticationProvider;
    private final UserRepository userRepository;
    private final com.example.thexuong.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Custom OAuth2UserService để đảm bảo principal.getName() trả về email
     * thay vì Google's sub (user ID).
     *
     * Khi đăng nhập Google, Spring Security mặc định dùng "sub" claim làm username.
     * Tuy nhiên, toàn bộ application (Cart, Order, User services) kỳ vọng
     * principal.getName() là email để query user từ database.
     *
     * Custom này:
     * 1. Lấy email từ Google OAuth2 response
     * 2. Tạo/cập nhật user với provider=GOOGLE
     * 3. Return DefaultOAuth2User với name = email (thay vì sub)
     */
    @Bean
    public DefaultOAuth2UserService oauth2UserService() {
        return new DefaultOAuth2UserService() {
            @Override
            public OAuth2User loadUser(OAuth2UserRequest userRequest) {
                // Gọi default service để lấy OAuth2User từ Google
                OAuth2User oAuth2User = super.loadUser(userRequest);

                // Lấy email từ attributes (Google gửi email trong claim "email")
                String email = oAuth2User.getAttribute("email");
                if (email == null) {
                    throw new IllegalStateException("Google OAuth2: Email not found in response");
                }

                // Lấy tên hiển thị
                String name = oAuth2User.getAttribute("name");

                // Đồng bộ user vào database (tạo mới nếu chưa có)
                User user = userRepository.findByEmail(email).orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .username(email) // username = email
                            .fullName(name)
                            .password("") // Google user không có password
                            .provider("GOOGLE")
                            .role("USER")
                            .active(true)
                            .build();
                    return userRepository.save(newUser);
                });

                // Lấy authorities từ user.role (USER / ADMIN / BOTH)
                String role = (user.getRole() == null || user.getRole().isBlank()) ? "USER" : user.getRole();
                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

                // Tạo DefaultOAuth2User với name = EMAIL (không phải sub)
                // Điều này đảm bảo authentication.getName() trả về email
                return new DefaultOAuth2User(
                        authorities,
                        oAuth2User.getAttributes(),
                        email // "name" attribute của OAuth2User = email
                );
            }
        };
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) -> {
            String path = request.getRequestURI();
            if (path.startsWith("/api/")) {
                if (!response.isCommitted()) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Unauthorized\"}");
                }
            } else {
                // Non-API: redirect to login page
                response.sendRedirect("/login");
            }
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Development: Vue dev server on port 5173
        // Production: Same origin (frontend served from this backend)
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",
            "http://localhost:8080",
            "http://127.0.0.1:5173",
            "http://127.0.0.1:8080",
            "https://thexuong.xuansown.id.vn"
        ));
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-CSRF-TOKEN"
        ));
        configuration.setExposedHeaders(Arrays.asList(
            "X-CSRF-TOKEN", "Set-Cookie"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        source.registerCorsConfiguration("/auth/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            if (!response.isCommitted()) {
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                response.setContentType("application/json");
                                response.getWriter().write("{\"error\":\"Forbidden\"}");
                            }
                        })
                )
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/forgot-password", "/api/v1/auth/reset-password", "/api/v1/auth/refresh").permitAll()
                .requestMatchers("/api/v1/products/**", "/api/v1/categories/**").permitAll()
                .requestMatchers("/api/v1/chatbot/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/reviews/product/**").permitAll()
                .requestMatchers("/api/v1/addresses", "/api/v1/addresses/**", "/api/v1/maps", "/api/v1/maps/**").authenticated()
                .requestMatchers("/api/v1/admin/**").hasAnyAuthority("ADMIN", "BOTH")
                .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
                .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo
                        .userService(oauth2UserService())
                )
                .successHandler(oAuth2SuccessHandler)
                );

        return http.build();
    }
}
