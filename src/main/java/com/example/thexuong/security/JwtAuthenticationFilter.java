package com.example.thexuong.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final JwtCookieService cookieService;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklist tokenBlacklist;

    private static final List<String> SKIP_PREFIXES = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/api/v1/products",
            "/api/v1/categories",
            "/api/v1/chatbot"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return SKIP_PREFIXES.stream().anyMatch(p -> path.equals(p) || path.startsWith(p + "/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String accessToken = cookieService.readCookie(request, "access_token");

        if (accessToken == null) {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                accessToken = header.substring(7);
            }
        }

        if (accessToken != null && jwtService.isValid(accessToken) && jwtService.isAccessToken(accessToken)) {
            setAuthentication(accessToken, request);
        } else if (accessToken != null && jwtService.isExpired(accessToken)) {
            String refreshToken = cookieService.readCookie(request, "refresh_token");
            if (refreshToken != null && jwtService.isValid(refreshToken) && jwtService.isRefreshToken(refreshToken)) {
                String username = jwtService.extractUsername(refreshToken);
                try {
                    UserDetails user = userDetailsService.loadUserByUsername(username);
                    String newAccess = jwtService.generateAccessToken(user);
                    String newRefresh = jwtService.generateRefreshToken(user);
                    cookieService.setAuthCookies(response, newAccess, newRefresh);
                    // ponytail: blacklist old refresh jti so it can't be reused
                    String oldJti = jwtService.extractClaims(refreshToken).getId();
                    java.time.Instant oldExp = jwtService.extractClaims(refreshToken).getExpiration().toInstant();
                    tokenBlacklist.blacklist(oldJti, oldExp);
                    setAuthentication(newAccess, request);
                    log.debug("[JWT] Auto-refreshed access token for user {}", username);
                } catch (Exception e) {
                    log.warn("[JWT] Refresh failed for user {}: {}", username, e.getMessage());
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String token, HttpServletRequest request) {
        try {
            String username = jwtService.extractUsername(token);
            UserDetails user = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    user, null, user.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception e) {
            log.warn("[JWT] setAuthentication failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
    }
}
