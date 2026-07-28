package com.example.thexuong.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtCookieService {

    private final String domain;
    private final boolean secure;
    private final String sameSite;
    @Value("${app.security.jwt.access-ttl-seconds:900}") private long accessTtlSeconds;
    @Value("${app.security.jwt.refresh-ttl-seconds:604800}") private long refreshTtlSeconds;

    public JwtCookieService(@Value("${app.security.jwt.cookie-domain:}") String domain,
                            @Value("${app.security.jwt.cookie-secure:true}") boolean secure,
                            @Value("${app.security.jwt.cookie-same-site:lax}") String sameSite) {
        this.domain = domain;
        this.secure = secure;
        this.sameSite = sameSite;
    }

    public void setAuthCookies(HttpServletResponse res, String accessToken, String refreshToken) {
        res.addHeader("Set-Cookie", buildCookie("access_token", accessToken, accessTtlSeconds));
        res.addHeader("Set-Cookie", buildCookie("refresh_token", refreshToken, refreshTtlSeconds));
    }

    public void clearAuthCookies(HttpServletResponse res) {
        res.addHeader("Set-Cookie", buildCookie("access_token", "", 0));
        res.addHeader("Set-Cookie", buildCookie("refresh_token", "", 0));
    }

    public String readCookie(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }

    private String buildCookie(String name, String value, long maxAgeSeconds) {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("=").append(value);
        sb.append("; Path=/");
        sb.append("; HttpOnly");
        if (secure) sb.append("; Secure");
        sb.append("; SameSite=").append(sameSite);
        if (maxAgeSeconds >= 0) sb.append("; Max-Age=").append(maxAgeSeconds);
        if (domain != null && !domain.isBlank()) sb.append("; Domain=").append(domain);
        return sb.toString();
    }
}
