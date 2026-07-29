package com.example.thexuong.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBlacklist {
    // ponytail: single-node in-memory; đổi Redis nếu chạy >1 BE instance
    private final Map<String, Instant> revokedJtis = new ConcurrentHashMap<>();

    public void blacklist(String jti, Instant exp) {
        if (jti == null || exp == null) return;
        revokedJtis.put(jti, exp);
    }

    public boolean isBlacklisted(String jti) {
        if (jti == null) return false;
        Instant exp = revokedJtis.get(jti);
        if (exp == null) return false;
        if (Instant.now().isAfter(exp)) {
            revokedJtis.remove(jti);
            return false;
        }
        return true;
    }

    @Scheduled(fixedDelay = 300_000) // 5 minutes
    public void cleanup() {
        Instant now = Instant.now();
        revokedJtis.entrySet().removeIf(e -> now.isAfter(e.getValue()));
    }
}
