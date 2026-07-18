package com.example.thexuong.security;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class JwtCookieServiceTest {

    private JwtCookieService cookieService;

    @BeforeEach
    void setUp() {
        cookieService = new JwtCookieService("", true, "lax");
    }

    @Test
    void setAuthCookies_addsBothCookies() {
        MockHttpServletResponse res = new MockHttpServletResponse();
        cookieService.setAuthCookies(res, "access123", "refresh456");
        assertThat(res.getCookies()).anySatisfy(c -> {
            assertThat(c.getName()).isEqualTo("access_token");
            assertThat(c.getValue()).isEqualTo("access123");
            assertThat(c.isHttpOnly()).isTrue();
            assertThat(c.getSecure()).isTrue();
            assertThat(c.getPath()).isEqualTo("/");
        });
        assertThat(res.getCookies()).anySatisfy(c -> {
            assertThat(c.getName()).isEqualTo("refresh_token");
            assertThat(c.getValue()).isEqualTo("refresh456");
        });
    }

    @Test
    void clearAuthCookies_setsMaxAgeZero() {
        MockHttpServletResponse res = new MockHttpServletResponse();
        cookieService.clearAuthCookies(res);
        assertThat(res.getCookies()).allSatisfy(c -> {
            assertThat(c.getMaxAge()).isEqualTo(0);
            assertThat(c.getValue()).isEmpty();
        });
    }

    @Test
    void readCookie_returnsValue_whenPresent() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new Cookie("access_token", "tok123"));
        assertThat(cookieService.readCookie(req, "access_token")).isEqualTo("tok123");
    }

    @Test
    void readCookie_returnsNull_whenAbsent() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        assertThat(cookieService.readCookie(req, "access_token")).isNull();
    }
}
