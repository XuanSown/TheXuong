package com.example.thexuong.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String jwt = null;
        String userEmail = null;

        // 1. tìm token trong cookie
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }
        //2. khoong thấy token thì cho qua
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        userEmail = jwtService.extractUsername(jwt);// Trong dự án này ta dùng email hoặc username làm subject

        // 3. Nếu có userEmail và chưa được xác thực trong Context hiện tại
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Lấy thông tin user từ db
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 4. Nếu token hợp lệ thì set Authentication cho spring security
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // cap nhat. Context: User này đã đăng nhập thành công
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // 5. Đi tiếp
        filterChain.doFilter(request, response);
    }
}
