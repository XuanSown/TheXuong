package com.example.thexuong.security;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private JwtCookieService cookieService;
    private UserDetailsService userDetailsService;
    private JwtAuthenticationFilter filter;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        TokenBlacklist blacklist = new TokenBlacklist();
        jwtService = new JwtService("test-secret-key-at-least-256-bits-long-xxxxxxxxxxxxxxxxxxxx", 900L, 604800L, blacklist);
        cookieService = new JwtCookieService("", true, "lax");
        userDetailsService = mock(UserDetailsService.class);
        filter = new JwtAuthenticationFilter(jwtService, cookieService, userDetailsService);
        userDetails = User.withUsername("user@test.com")
                .password("dummy")
                .authorities(List.of(() -> "CUSTOMER"))
                .build();
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_validAccessToken_setsAuthentication() throws Exception {
        String token = jwtService.generateAccessToken(userDetails);
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new Cookie("access_token", token));
        req.setRequestURI("/api/v1/cart");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("user@test.com");
    }

    @Test
    void doFilter_noCookie_doesNotSetAuthentication() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/api/v1/cart");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilter_permitAllPath_skipsFilter() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/api/v1/products");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void doFilter_garbageToken_doesNotSetAuthentication() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new Cookie("access_token", "garbage.token.here"));
        req.setRequestURI("/api/v1/cart");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
