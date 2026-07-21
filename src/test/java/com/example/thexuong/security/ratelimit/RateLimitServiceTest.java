package com.example.thexuong.security.ratelimit;

import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RateLimitServiceTest {
    @Test
    public void testResolveBucket() {
        RateLimitService service = new RateLimitService();
        Bucket bucket1 = service.resolveBucket("ip1", RateLimitPlan.AUTH_REGISTER);
        Bucket bucket2 = service.resolveBucket("ip1", RateLimitPlan.AUTH_REGISTER);
        Bucket bucket3 = service.resolveBucket("ip2", RateLimitPlan.AUTH_REGISTER);
        
        assertSame(bucket1, bucket2, "Should return same bucket for same key");
        assertNotSame(bucket1, bucket3, "Should return different bucket for different key");
    }
}
