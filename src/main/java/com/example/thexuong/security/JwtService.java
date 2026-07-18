package com.example.thexuong.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class JwtService {

    private static final String DEV_SECRET = "dev-only-secret-key-change-in-prod-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";

    private final SecretKey signingKey;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;
    private final TokenBlacklist blacklist;

    public JwtService(@Value("${app.security.jwt.secret}") String secret,
                      @Value("${app.security.jwt.access-ttl-seconds:900}") long accessTtlSeconds,
                      @Value("${app.security.jwt.refresh-ttl-seconds:604800}") long refreshTtlSeconds,
                      TokenBlacklist blacklist) {
        if (secret == null || secret.length() < 32) {
            log.warn("[JWT] Secret too short or null — using insecure dev key. Set JWT_SECRET env var in production!");
            secret = DEV_SECRET;
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
        this.blacklist = blacklist;
    }

    public String generateAccessToken(UserDetails user) {
        return buildToken(user, "access", accessTtlSeconds);
    }

    public String generateRefreshToken(UserDetails user) {
        return buildToken(user, "refresh", refreshTtlSeconds);
    }

    private String buildToken(UserDetails user, String type, long ttlSeconds) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + ttlSeconds * 1000);
        String role = user.getAuthorities().stream().findFirst()
                .map(GrantedAuthority::getAuthority).orElse("CUSTOMER");
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("type", type)
                .claim("role", role)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isAccessToken(String token) {
        try {
            return "access".equals(extractClaims(token).get("type"));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            return "refresh".equals(extractClaims(token).get("type"));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isExpired(String token) {
        try {
            return extractClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isJtiBlacklisted(String token) {
        try {
            String jti = extractClaims(token).getId();
            return blacklist.isBlacklisted(jti);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isValid(String token) {
        try {
            extractClaims(token);
            return !isExpired(token) && !isJtiBlacklisted(token);
        } catch (Exception e) {
            return false;
        }
    }
}
