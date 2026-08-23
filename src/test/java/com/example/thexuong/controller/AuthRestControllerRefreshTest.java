package com.example.thexuong.controller;

import com.example.thexuong.security.JwtCookieService;
import com.example.thexuong.security.JwtService;
import com.example.thexuong.security.TokenBlacklist;
import com.example.thexuong.service.LoginHistoryService;
import com.example.thexuong.service.PasswordResetService;
import com.example.thexuong.service.UserService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthRestControllerRefreshTest {

    @Test
    void refresh_returns423WhenUserLocked() {
        JwtService jwtService = mock(JwtService.class);
        JwtCookieService cookieService = mock(JwtCookieService.class);
        TokenBlacklist tokenBlacklist = mock(TokenBlacklist.class);
        UserDetailsService userDetailsService = mock(UserDetailsService.class);

        AuthRestController controller = new AuthRestController(
                mock(AuthenticationManager.class),
                mock(UserService.class),
                mock(PasswordResetService.class),
                mock(LoginHistoryService.class),
                jwtService,
                cookieService,
                tokenBlacklist,
                userDetailsService);

        when(cookieService.readCookie(any(), eq("refresh_token"))).thenReturn("refresh-token");
        when(jwtService.isValid("refresh-token")).thenReturn(true);
        when(jwtService.isRefreshToken("refresh-token")).thenReturn(true);
        when(jwtService.extractUsername("refresh-token")).thenReturn("locked@test.com");

        Claims claims = mock(Claims.class);
        Date expiration = Date.from(Instant.now().plusSeconds(3600));
        when(claims.getId()).thenReturn("jti-123");
        when(claims.getExpiration()).thenReturn(expiration);
        when(jwtService.extractClaims("refresh-token")).thenReturn(claims);

        UserDetails locked = org.springframework.security.core.userdetails.User.withUsername("locked@test.com")
                .password("")
                .disabled(true)
                .authorities(List.of(new SimpleGrantedAuthority("CUSTOMER")))
                .build();
        when(userDetailsService.loadUserByUsername("locked@test.com")).thenReturn(locked);

        MockHttpServletResponse response = new MockHttpServletResponse();
        ResponseEntity<?> resp = controller.refresh(new MockHttpServletRequest(), response);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.LOCKED);
        Map<?, ?> body = (Map<?, ?>) resp.getBody();
        assertThat(body.get("error")).isEqualTo("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
        verify(tokenBlacklist).blacklist("jti-123", expiration.toInstant());
        verify(cookieService).clearAuthCookies(response);
    }
}
