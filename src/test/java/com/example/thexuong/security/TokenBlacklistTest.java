package com.example.thexuong.security;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TokenBlacklistTest {

    @Test
    void blacklist_then_isBlacklisted_returnsTrue() {
        TokenBlacklist bl = new TokenBlacklist();
        String jti = "test-jti-123";
        bl.blacklist(jti, Instant.now().plusSeconds(60));
        assertThat(bl.isBlacklisted(jti)).isTrue();
    }

    @Test
    void isBlacklisted_unknownJti_returnsFalse() {
        TokenBlacklist bl = new TokenBlacklist();
        assertThat(bl.isBlacklisted("unknown")).isFalse();
    }

    @Test
    void isBlacklisted_nullJti_returnsFalse() {
        TokenBlacklist bl = new TokenBlacklist();
        assertThat(bl.isBlacklisted(null)).isFalse();
    }

    @Test
    void blacklist_nullArgs_isNoOp() {
        TokenBlacklist bl = new TokenBlacklist();
        bl.blacklist(null, Instant.now().plusSeconds(60));
        bl.blacklist("jti", null);
        assertThat(bl.isBlacklisted(null)).isFalse();
    }

    @Test
    void isBlacklisted_expiredEntry_returnsFalse_and_evicts() {
        TokenBlacklist bl = new TokenBlacklist();
        String jti = "expired-jti";
        bl.blacklist(jti, Instant.now().minusSeconds(60));
        assertThat(bl.isBlacklisted(jti)).isFalse();
    }

    @Test
    void cleanup_removes_expired_entries() {
        TokenBlacklist bl = new TokenBlacklist();
        bl.blacklist("expired-1", Instant.now().minusSeconds(60));
        bl.blacklist("valid-1", Instant.now().plusSeconds(60));
        bl.cleanup();
        assertThat(bl.isBlacklisted("expired-1")).isFalse();
        assertThat(bl.isBlacklisted("valid-1")).isTrue();
    }
}
