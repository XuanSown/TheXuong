package com.example.thexuong.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private TokenBlacklist blacklist;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        blacklist = new TokenBlacklist();
        jwtService = new JwtService(
                "test-secret-key-at-least-256-bits-long-xxxxxxxxxxxxxxxxxxxx",
                900L,
                604800L,
                blacklist
        );
        userDetails = User.withUsername("user@test.com")
                .password("dummy")
                .authorities(List.of(() -> "CUSTOMER"))
                .build();
    }

    @Test
    void generateAccessToken_hasCorrectClaims() {
        String token = jwtService.generateAccessToken(userDetails);
        Claims claims = jwtService.extractClaims(token);
        assertThat(claims.getSubject()).isEqualTo("user@test.com");
        assertThat(claims.get("type")).isEqualTo("access");
        assertThat(claims.getId()).isNotNull();
        assertThat(claims.get("role")).isEqualTo("CUSTOMER");
    }

    @Test
    void generateRefreshToken_hasTypeRefresh() {
        String token = jwtService.generateRefreshToken(userDetails);
        Claims claims = jwtService.extractClaims(token);
        assertThat(claims.get("type")).isEqualTo("refresh");
    }

    @Test
    void isAccessToken_true_forAccess_false_forRefresh() {
        String access = jwtService.generateAccessToken(userDetails);
        String refresh = jwtService.generateRefreshToken(userDetails);
        assertThat(jwtService.isAccessToken(access)).isTrue();
        assertThat(jwtService.isAccessToken(refresh)).isFalse();
    }

    @Test
    void isValid_true_forFreshToken_false_forGarbage() {
        String token = jwtService.generateAccessToken(userDetails);
        assertThat(jwtService.isValid(token)).isTrue();
        assertThat(jwtService.isValid("garbage.token.here")).isFalse();
    }

    @Test
    void isValid_false_whenJtiBlacklisted() {
        String token = jwtService.generateAccessToken(userDetails);
        Claims claims = jwtService.extractClaims(token);
        blacklist.blacklist(claims.getId(), claims.getExpiration().toInstant());
        assertThat(jwtService.isValid(token)).isFalse();
    }

    @Test
    void extractUsername_returnsSubject() {
        String token = jwtService.generateAccessToken(userDetails);
        assertThat(jwtService.extractUsername(token)).isEqualTo("user@test.com");
    }
}
