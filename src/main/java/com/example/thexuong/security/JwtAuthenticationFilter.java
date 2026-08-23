package com.example.thexuong.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    private static final String LOCKED_MESSAGE = "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.";

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
        String refreshToken = cookieService.readCookie(request, "refresh_token");

        if (accessToken != null && jwtService.isValid(accessToken) && jwtService.isAccessToken(accessToken)) {
            UserDetails user = tryLoadUser(jwtService.extractUsername(accessToken));
            if (user == null) {
                filterChain.doFilter(request, response);
                return;
            }
            if (!user.isEnabled()) {
                log.warn("[JWT] Rejected request from locked user {}", user.getUsername());
                kickLockedUser(response, accessToken, refreshToken);
                return;
            }
            setAuthentication(user, request);
        } else if (accessToken != null && jwtService.isExpired(accessToken)
                && refreshToken != null && jwtService.isValid(refreshToken) && jwtService.isRefreshToken(refreshToken)) {
            String username = jwtService.extractUsername(refreshToken);
            UserDetails user = tryLoadUser(username);
            if (user == null) {
                filterChain.doFilter(request, response);
                return;
            }
            if (!user.isEnabled()) {
                log.warn("[JWT] Rejected auto-refresh for locked user {}", username);
                kickLockedUser(response, accessToken, refreshToken);
                return;
            }
            String newAccess = jwtService.generateAccessToken(user);
            String newRefresh = jwtService.generateRefreshToken(user);
            cookieService.setAuthCookies(response, newAccess, newRefresh);
            // ponytail: blacklist old refresh jti so it can't be reused
            String oldJti = jwtService.extractClaims(refreshToken).getId();
            java.time.Instant oldExp = jwtService.extractClaims(refreshToken).getExpiration().toInstant();
            tokenBlacklist.blacklist(oldJti, oldExp);
            setAuthentication(user, request);
            log.debug("[JWT] Auto-refreshed access token for user {}", username);
        }

        filterChain.doFilter(request, response);
    }

    private UserDetails tryLoadUser(String username) {
        try {
            return userDetailsService.loadUserByUsername(username);
        } catch (Exception e) {
            log.warn("[JWT] Load user failed for {}: {}", username, e.getMessage());
            SecurityContextHolder.clearContext();
            return null;
        }
    }

    private void setAuthentication(UserDetails user, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void kickLockedUser(HttpServletResponse response, String accessToken, String refreshToken)
            throws IOException {
        SecurityContextHolder.clearContext();
        blacklistToken(accessToken);
        blacklistToken(refreshToken);
        cookieService.clearAuthCookies(response);
        if (!response.isCommitted()) {
            response.setStatus(HttpStatus.LOCKED.value());
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"" + LOCKED_MESSAGE + "\"}");
        }
    }

    private void blacklistToken(String token) {
        if (token == null) return;
        try {
            var claims = jwtService.extractClaims(token);
            tokenBlacklist.blacklist(claims.getId(), claims.getExpiration().toInstant());
        } catch (Exception ignored) {
            log.debug("[JWT] Không blacklist được token: {}", ignored.getMessage());
        }
    }
}
