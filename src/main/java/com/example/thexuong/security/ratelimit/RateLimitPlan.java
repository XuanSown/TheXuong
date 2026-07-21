package com.example.thexuong.security.ratelimit;

import java.time.Duration;

public enum RateLimitPlan {
    AUTH_LOGIN(5, Duration.ofMinutes(15)),
    AUTH_REGISTER(3, Duration.ofHours(1)),
    AUTH_FORGOT_PASSWORD(3, Duration.ofHours(1)),
    AUTH_RESET_PASSWORD(5, Duration.ofHours(1)),
    AUTH_REFRESH_TOKEN(10, Duration.ofMinutes(1)),
    USER_ORDER(5, Duration.ofMinutes(1)),
    USER_PAYMENT(5, Duration.ofMinutes(1)),
    USER_REVIEW(10, Duration.ofMinutes(1)),
    USER_COMMENT(10, Duration.ofMinutes(1)),
    USER_PROFILE(5, Duration.ofMinutes(1)),
    PUBLIC_PRODUCT(60, Duration.ofMinutes(1)),
    GLOBAL(300, Duration.ofMinutes(1));

    private final int limit;
    private final Duration duration;

    RateLimitPlan(int limit, Duration duration) {
        this.limit = limit;
        this.duration = duration;
    }

    public int getLimit() { return limit; }
    public Duration getDuration() { return duration; }
}
