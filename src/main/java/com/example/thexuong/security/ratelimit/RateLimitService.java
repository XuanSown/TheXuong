package com.example.thexuong.security.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key, RateLimitPlan plan) {
        return cache.computeIfAbsent(key, k -> createNewBucket(plan));
    }

    private Bucket createNewBucket(RateLimitPlan plan) {
        Refill refill = Refill.intervally(plan.getLimit(), plan.getDuration());
        Bandwidth limit = Bandwidth.classic(plan.getLimit(), refill);
        return Bucket.builder().addLimit(limit).build();
    }
}
