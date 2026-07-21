# API Rate Limiting Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement dynamic API rate limiting using bucket4j-core and a custom Spring Boot HandlerInterceptor to protect endpoints based on IP address and authenticated User IDs.

**Architecture:** Use `bucket4j-core` to create in-memory token buckets for rate limiting. An enum `RateLimitPlan` defines the limits. `RateLimitService` manages a `ConcurrentHashMap` of active buckets. `RateLimitInterceptor` intercepts all `/api/**` requests, determines the target bucket based on URL and JWT context, and rejects excess traffic with HTTP 429.

**Tech Stack:** Java 21, Spring Boot 3.5, Bucket4j Core, JUnit 5.

## Global Constraints

- Use `com.bucket4j:bucket4j-core:8.10.0` (Do not use bucket4j-spring-boot-starter).
- Limit requests using in-memory ConcurrentHashMap without external caches like Redis. *(Lưu ý: ConcurrentHashMap có thể gây leak memory nếu có quá nhiều IP/User truy cập, ở môi trường thực tế nên dùng Caffeine Cache).*
- Rate limiting must target User ID for authenticated transaction endpoints (orders, payments, reviews) and fallback to IP for public/auth endpoints. `auth.getName()` trả về **principal name** (email trong codebase này) — dùng làm key cho USER_* plans.
- Return HTTP 429 status with JSON message on rejection.
- Maintain existing codebase style (Java 21, Spring annotations).
- **Double-count có chủ ý**: mỗi request `/api/**` trừ 1 token ở specific bucket (nếu không phải GLOBAL) + 1 token ở global bucket. GLOBAL đóng vai trò **per-IP cap** (mỗi IP có bucket global riêng 300/phút) — không phải app-wide circuit breaker, tránh 1 IP choke app. Không phải bug, ghi chú rõ trong code.
- **`Refill.intervally` = fixed window**: bucket nạp lại toàn bộ capacity sau mỗi `duration` (burst rồi chờ). Chấp nhận ceiling này; `Refill.greedy` mượt hơn nhưng phức tạp hơn — ponytail: dùng `intervally`, chuyển `greedy` nếu cần trải đều traffic.

---

### Task 1: Add Bucket4j Dependency

**Files:**
- Modify: `build.gradle`

**Interfaces:**
- Consumes: None
- Produces: `io.github.bucket4j.Bucket` (available on classpath)

- [ ] **Step 1: Write the failing test**
*(Skipped for dependency addition)*

- [ ] **Step 2: Write minimal implementation**
Open `build.gradle` and append the bucket4j dependency to the `dependencies { ... }` block:
```groovy
    implementation 'com.bucket4j:bucket4j-core:8.10.0'
```

- [ ] **Step 3: Run build to verify resolution**
Run: `./gradlew dependencies | grep bucket4j`
Expected: Output showing `bucket4j-core:8.10.0`.

- [ ] **Step 4: Commit**
```bash
git add build.gradle
git commit -m "chore: add bucket4j-core dependency"
```

---

### Task 2: Create RateLimitPlan Enum

**Files:**
- Create: `src/main/java/com/example/thexuong/security/ratelimit/RateLimitPlan.java`
- Create: `src/test/java/com/example/thexuong/security/ratelimit/RateLimitPlanTest.java`

**Interfaces:**
- Consumes: None
- Produces: `RateLimitPlan` enum with `getLimit()` and `getDuration()`

- [ ] **Step 1: Write the failing test**
Create `src/test/java/com/example/thexuong/security/ratelimit/RateLimitPlanTest.java`
```java
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
```

- [ ] **Step 2: Run test to verify it fails**
Run: `./gradlew test --tests "*RateLimitPlanTest*"`
Expected: FAIL due to missing class.

- [ ] **Step 3: Write minimal implementation**
Create `src/main/java/com/example/thexuong/security/ratelimit/RateLimitPlan.java`
```java
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
```

- [ ] **Step 4: Run test to verify it passes**
Run: `./gradlew test --tests "*RateLimitPlanTest*"`
Expected: PASS.

- [ ] **Step 5: Commit**
```bash
git add src/main/java/com/example/thexuong/security/ratelimit/RateLimitPlan.java src/test/java/com/example/thexuong/security/ratelimit/RateLimitPlanTest.java
git commit -m "feat: define RateLimitPlan enum"
```

---

### Task 3: Create RateLimitService

**Files:**
- Create: `src/main/java/com/example/thexuong/security/ratelimit/RateLimitService.java`
- Create: `src/test/java/com/example/thexuong/security/ratelimit/RateLimitServiceTest.java`

**Interfaces:**
- Consumes: `RateLimitPlan`
- Produces: `Bucket resolveBucket(String key, RateLimitPlan plan)`

- [ ] **Step 1: Write the failing test**
Create `src/test/java/com/example/thexuong/security/ratelimit/RateLimitServiceTest.java`
```java
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
```

- [ ] **Step 2: Run test to verify it fails**
Run: `./gradlew test --tests "*RateLimitServiceTest*"`
Expected: FAIL due to missing class.

- [ ] **Step 3: Write minimal implementation**
Create `src/main/java/com/example/thexuong/security/ratelimit/RateLimitService.java`
```java
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
```

- [ ] **Step 4: Run test to verify it passes**
Run: `./gradlew test --tests "*RateLimitServiceTest*"`
Expected: PASS.

- [ ] **Step 5: Commit**
```bash
git add src/main/java/com/example/thexuong/security/ratelimit/RateLimitService.java src/test/java/com/example/thexuong/security/ratelimit/RateLimitServiceTest.java
git commit -m "feat: add RateLimitService to manage token buckets"
```

---

### Task 4: Create RateLimitInterceptor

**Files:**
- Create: `src/main/java/com/example/thexuong/security/ratelimit/RateLimitInterceptor.java`
- Create: `src/test/java/com/example/thexuong/security/ratelimit/RateLimitInterceptorTest.java`

**Interfaces:**
- Consumes: `RateLimitService`, `RateLimitPlan`
- Produces: `HandlerInterceptor` implementation that applies rate limiting

- [ ] **Step 1: Write the failing test**
Create `src/test/java/com/example/thexuong/security/ratelimit/RateLimitInterceptorTest.java`
```java
package com.example.thexuong.security.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

public class RateLimitInterceptorTest {
    @Test
    public void testRegisterRateLimiting() throws Exception {
        RateLimitService service = new RateLimitService();
        RateLimitInterceptor interceptor = new RateLimitInterceptor(service);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/api/v1/auth/register");
        req.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse res = new MockHttpServletResponse();

        // 3 requests allowed for AUTH_REGISTER
        assertTrue(interceptor.preHandle(req, res, null));
        assertTrue(interceptor.preHandle(req, res, null));
        assertTrue(interceptor.preHandle(req, res, null));

        // 4th request should fail
        assertFalse(interceptor.preHandle(req, res, null));
        assertEquals(429, res.getStatus());
    }

    @Test
    public void testLoginRateLimiting() throws Exception {
        RateLimitService service = new RateLimitService();
        RateLimitInterceptor interceptor = new RateLimitInterceptor(service);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/api/v1/auth/login");
        req.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse res = new MockHttpServletResponse();

        // 5 requests allowed for AUTH_LOGIN
        for (int i = 0; i < 5; i++) {
            assertTrue(interceptor.preHandle(req, res, null));
        }

        // 6th request should fail
        assertFalse(interceptor.preHandle(req, res, null));
        assertEquals(429, res.getStatus());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**
Run: `./gradlew test --tests "*RateLimitInterceptorTest*"`
Expected: FAIL due to missing class.

- [ ] **Step 3: Write minimal implementation**
Create `src/main/java/com/example/thexuong/security/ratelimit/RateLimitInterceptor.java`
```java
package com.example.thexuong.security.ratelimit;

import com.example.thexuong.util.ClientIpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    private final RateLimitService rateLimitService;

    @Autowired
    public RateLimitInterceptor(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        if (!uri.startsWith("/api/")) {
            return true;
        }

        RateLimitPlan plan = determinePlan(uri);
        String clientIp = ClientIpUtil.getClientIp(request);
        String key = determineKey(clientIp, plan);

        // 1. Check Specific Limit (if not global fallback)
        // ponytail: double-count với global là circuit breaker có chủ ý, không phải bug
        if (plan != RateLimitPlan.GLOBAL) {
            Bucket specificBucket = rateLimitService.resolveBucket(key, plan);
            ConsumptionProbe specificProbe = specificBucket.tryConsumeAndReturnRemaining(1);
            if (!specificProbe.isConsumed()) {
                reject429(response);
                return false;
            }
        }

        // 2. Check Global Limit (always check for all /api/**)
        String globalKey = "global_" + clientIp;
        Bucket globalBucket = rateLimitService.resolveBucket(globalKey, RateLimitPlan.GLOBAL);
        ConsumptionProbe globalProbe = globalBucket.tryConsumeAndReturnRemaining(1);
        if (!globalProbe.isConsumed()) {
            reject429(response);
            return false;
        }

        return true;
    }

    private void reject429(HttpServletResponse response) throws Exception {
        // ponytail: tên khác với HttpServletResponse.sendError để tránh nhầm
        response.setStatus(429);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"status\": 429, \"message\": \"Quá nhiều yêu cầu, vui lòng thử lại sau.\"}");
    }

    private RateLimitPlan determinePlan(String uri) {
        if (uri.startsWith("/api/v1/auth/login")) return RateLimitPlan.AUTH_LOGIN;
        if (uri.startsWith("/api/v1/auth/register")) return RateLimitPlan.AUTH_REGISTER;
        if (uri.startsWith("/api/v1/auth/forgot-password")) return RateLimitPlan.AUTH_FORGOT_PASSWORD;
        if (uri.startsWith("/api/v1/auth/reset-password")) return RateLimitPlan.AUTH_RESET_PASSWORD;
        if (uri.startsWith("/api/v1/auth/refresh-token")) return RateLimitPlan.AUTH_REFRESH_TOKEN;
        if (uri.startsWith("/api/v1/orders")) return RateLimitPlan.USER_ORDER;
        if (uri.startsWith("/api/v1/payments")) return RateLimitPlan.USER_PAYMENT;
        if (uri.startsWith("/api/v1/reviews")) return RateLimitPlan.USER_REVIEW;
        if (uri.startsWith("/api/v1/comments")) return RateLimitPlan.USER_COMMENT;
        if (uri.startsWith("/api/v1/users/me")) return RateLimitPlan.USER_PROFILE;
        if (uri.startsWith("/api/v1/products")) return RateLimitPlan.PUBLIC_PRODUCT;
        return RateLimitPlan.GLOBAL;
    }

    private String determineKey(String clientIp, RateLimitPlan plan) {
        // USER_* plans: key theo principal name (email trong codebase này) nếu đã auth,
        // fallback IP nếu anonymous. Các plan khác: key theo IP.
        boolean requiresUser = plan.name().startsWith("USER_");
        if (requiresUser) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return plan.name() + "_" + auth.getName();
            }
        }
        return plan.name() + "_" + clientIp;
    }
}
```

- [ ] **Step 4: Run test to verify it passes**
Run: `./gradlew test --tests "*RateLimitInterceptorTest*"`
Expected: PASS.

- [ ] **Step 5: Commit**
```bash
git add src/main/java/com/example/thexuong/security/ratelimit/RateLimitInterceptor.java src/test/java/com/example/thexuong/security/ratelimit/RateLimitInterceptorTest.java
git commit -m "feat: add RateLimitInterceptor"
```

---

### Task 5: Register Interceptor in WebMvcConfig

**Files:**
- Create: `src/main/java/com/example/thexuong/config/RateLimitConfig.java`

**Interfaces:**
- Consumes: `RateLimitInterceptor`
- Produces: Spring Configuration that registers the interceptor.

- [ ] **Step 1: Write the failing test**
*(Integration configuration - test skipped, manual application start testing is preferred)*

- [ ] **Step 2: Write minimal implementation**
Create `src/main/java/com/example/thexuong/config/RateLimitConfig.java`
```java
package com.example.thexuong.config;

import com.example.thexuong.security.ratelimit.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RateLimitConfig implements WebMvcConfigurer {
    private final RateLimitInterceptor rateLimitInterceptor;

    @Autowired
    public RateLimitConfig(RateLimitInterceptor rateLimitInterceptor) {
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/api/**");
    }
}
```

- [ ] **Step 3: Run the application locally to verify**
Run: `./gradlew bootRun`
Expected: Application starts successfully without errors. Ensure you can hit a public endpoint (e.g., `/api/products`) and after 61 fast requests, it should return 429.

- [ ] **Step 4: Commit**
```bash
git add src/main/java/com/example/thexuong/config/RateLimitConfig.java
git commit -m "config: register RateLimitInterceptor"
```
