package com.example.thexuong.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final JwtCookieService cookieService = mock(JwtCookieService.class);
    private final UserDetailsService userDetailsService = mock(UserDetailsService.class);
    private final TokenBlacklist tokenBlacklist = mock(TokenBlacklist.class);
    private final FilterChain chain = mock(FilterChain.class);

    private final JwtAuthenticationFilter filter =
            new JwtAuthenticationFilter(jwtService, cookieService, userDetailsService, tokenBlacklist);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private UserDetails user(String email, boolean enabled) {
        return org.springframework.security.core.userdetails.User.withUsername(email)
                .password("")
                .disabled(!enabled)
                .authorities(List.of(new SimpleGrantedAuthority("CUSTOMER")))
                .build();
    }

    private MockHttpServletRequest requestWithTokens() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        when(cookieService.readCookie(request, "access_token")).thenReturn("access-token");
        when(cookieService.readCookie(request, "refresh_token")).thenReturn("refresh-token");
        return request;
    }

    @Test
    void validTokenEnabledUser_setsAuthenticationAndContinues() throws Exception {
        MockHttpServletRequest request = requestWithTokens();
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isValid("access-token")).thenReturn(true);
        when(jwtService.isAccessToken("access-token")).thenReturn(true);
        when(jwtService.extractUsername("access-token")).thenReturn("user@test.com");
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(user("user@test.com", true));

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
        verify(chain).doFilter(request, response);
        verify(cookieService, never()).clearAuthCookies(any());
    }

    @Test
    void validTokenLockedUser_returns423BlacklistsAndClearsCookies() throws Exception {
        MockHttpServletRequest request = requestWithTokens();
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isValid("access-token")).thenReturn(true);
        when(jwtService.isAccessToken("access-token")).thenReturn(true);
        when(jwtService.extractUsername("access-token")).thenReturn("locked@test.com");
        when(userDetailsService.loadUserByUsername("locked@test.com")).thenReturn(user("locked@test.com", false));

        Claims claims = mock(Claims.class);
        when(claims.getId()).thenReturn("jti-x");
        when(claims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 3600_000));
        when(jwtService.extractClaims(anyString())).thenReturn(claims);

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(423);
        assertThat(response.getContentAsString()).contains("Tài khoản của bạn đã bị khóa");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain, never()).doFilter(any(), any());
        verify(cookieService).clearAuthCookies(response);
        verify(tokenBlacklist, times(2)).blacklist(anyString(), any(Instant.class));
    }

    @Test
    void expiredAccessLockedUser_autoRefreshRejectedWith423() throws Exception {
        MockHttpServletRequest request = requestWithTokens();
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isValid("access-token")).thenReturn(false);
        when(jwtService.isExpired("access-token")).thenReturn(true);
        when(jwtService.isValid("refresh-token")).thenReturn(true);
        when(jwtService.isRefreshToken("refresh-token")).thenReturn(true);
        when(jwtService.extractUsername("refresh-token")).thenReturn("locked@test.com");
        when(userDetailsService.loadUserByUsername("locked@test.com")).thenReturn(user("locked@test.com", false));

        Claims claims = mock(Claims.class);
        when(claims.getId()).thenReturn("jti-x");
        when(claims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 3600_000));
        when(jwtService.extractClaims(anyString())).thenReturn(claims);

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(423);
        verify(chain, never()).doFilter(any(), any());
        verify(cookieService, never()).setAuthCookies(any(), anyString(), anyString());
        verify(cookieService).clearAuthCookies(response);
    }
}
