package com.example.thexuong.config;

import com.example.thexuong.security.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Bật @PreAuthorize/@PostAuthorize cho Method Security
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // 1. Cho phép truy cập resources
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/fonts/**", "/uploads/**").permitAll()

                        // 2. Các trang Public ai cũng xem được
                        .requestMatchers("/", "/index", "/login", "/register", "/products/**", "/product-detail/**", "/forgot-password", "/vnpay-return").permitAll()

                        // 3. CHẶN ADMIN: Tránh trường hợp Admin gõ URL vào trang của Khách
                        .requestMatchers("/cart/**", "/checkout/**", "/orders/**", "/profile/**", "/place-order", "/order/**").hasAnyAuthority("USER", "BOTH")

                        // 4. CHỈ ADMIN và BOTH mới vào được hệ thống quản trị (Thymeleaf + REST API)
                        .requestMatchers("/admin/**", "/api/admin/**").hasAnyAuthority("ADMIN", "BOTH")

                        .anyRequest().authenticated()
                )

                .authenticationProvider(authenticationProvider)

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/perform_login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .permitAll()
                )

                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .successHandler(oAuth2SuccessHandler)
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }
}