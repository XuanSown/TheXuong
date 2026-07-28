package com.example.thexuong.security.ratelimit;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

public class RateLimitPlanTest {
    @Test
    public void testPlanValues() {
        assertEquals(5, RateLimitPlan.AUTH_LOGIN.getLimit());
        assertEquals(Duration.ofMinutes(15), RateLimitPlan.AUTH_LOGIN.getDuration());
        assertEquals(3, RateLimitPlan.AUTH_REGISTER.getLimit());
        assertEquals(Duration.ofHours(1), RateLimitPlan.AUTH_REGISTER.getDuration());
        assertEquals(300, RateLimitPlan.GLOBAL.getLimit());
    }
}
