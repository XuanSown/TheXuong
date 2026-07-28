package com.example.thexuong.security.ratelimit;

import io.github.bucket4j.Bucket;
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
        return Bucket.builder()
                .addLimit(limit -> limit
                        .capacity(plan.getLimit())
                        .refillIntervally(plan.getLimit(), plan.getDuration()))
                .build();
    }
}
