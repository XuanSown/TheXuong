# JWT + Rate Limit Fix Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate auth từ session sang JWT (httpOnly cookie, stateless refresh) + fix rate limit dead code.

**Architecture:** JWT trong httpOnly cookie (`access_token` 15m + `refresh_token` 7d, stateless — refresh là JWT có claim `type=refresh`). `JwtAuthenticationFilter` đọc cookie, set SecurityContext, auto-refresh access khi hết hạn. Logout = in-memory jti blacklist (`ConcurrentHashMap` + `@Scheduled` cleanup, mất khi restart — chấp nhận). Bỏ formLogin/session, `SessionCreationPolicy.STATELESS`. Google OAuth2 issue JWT sau callback.

**Tech Stack:** Spring Boot 3.5.9, Java 21, Spring Security 6.x, jjwt 0.11.5 (dep đã có trong `build.gradle:90-92`), Vue 3 + axios.

## Global Constraints

- **NO DB tables** — refresh/blacklist in-memory only (`spring.jpa.hibernate.ddl-auto=none`).
- **JWT transport:** httpOnly cookie (`SameSite=Lax`, `Secure=true`, path=`/`), KHÔNG phải `Authorization: Bearer` header (trừ fallback cho Postman/test).
- **Algorithm:** HS256, key 256-bit từ `${app.security.jwt.secret}`.
- **Token TTL:** access 900s (15m), refresh 604800s (7d) — override qua `application.properties`.
- **JWT secret:** env var `JWT_SECRET`, dev fallback key (log warning).
- **jjwt 0.11.5** đã có dep — KHÔNG thêm dependency mới.
- **FE giữ `withCredentials:true`**, bỏ CSRF interceptor (không còn cần với JWT cookie).
- **AGENTS.md:** FE dùng relative path `/api/v1` qua Vite proxy (đã tuân thủ, không đụng).
- **Reuse:** `UserDetailsService` bean (ApplicationConfig:32), `LoginRateLimitFilter` (đã có), `ApiResponse` DTO (GlobalExceptionHandler).
- **Ponytail:** shortest diff, no new interface/factory cho 1 implementation, no speculative abstraction.
- Java 21 + Spring Security 6.x lambda DSL (đã dùng trong SecurityConfig).
- **Build command:** `./gradlew.bat build -x test` (Windows), `./gradlew.bat test` cho test.
- **Commit message style:** xem `git log --oneline -5` trước khi commit task đầu.

**Key file paths (absolute):**
- BE root: `D:\FPT Polytechnic\JAVA\JAVA5\TheXuong\src\main\java\com\example\thexuong`
- Config: `...\config\SecurityConfig.java`, `...\config\ApplicationConfig.java`
- Filter: `...\filter\LoginRateLimitFilter.java`
- Controller: `...\controller\AuthRestController.java`
- Security: `...\security\OAuth2SuccessHandler.java` (tạo mới: `...\security\JwtService.java`, `TokenBlacklist.java`, `JwtCookieService.java`, `JwtAuthenticationFilter.java`)
- Properties: `D:\...\src\main\resources\application.properties`
- FE: `D:\...\frontend\src\services\http.ts`, `...\frontend\src\stores\auth.store.ts`, `...\frontend\index.html`

---

## File Structure

### Files mới (4)

| File | Responsibility |
|---|---|
| `security/JwtService.java` | Generate/parse/validate JWT (access + refresh), extract claims, check blacklist. Stateless. |
| `security/TokenBlacklist.java` | In-memory jti blacklist với `@Scheduled` cleanup. |
| `security/JwtCookieService.java` | Set/clear httpOnly cookie cho access + refresh token. |
| `security/JwtAuthenticationFilter.java` | `OncePerRequestFilter` đọc cookie, set SecurityContext, auto-refresh access. |

### Files sửa (6)

| File | Thay đổi |
|---|---|
| `controller/AuthRestController.java` | login: issue JWT + catch `AuthenticationException` record rate limit. logout: blacklist jti + clear cookie. NEW `/refresh` endpoint. |
| `config/SecurityConfig.java` | `addFilterBefore(jwtFilter)`, `sessionManagement(STATELESS)`, bỏ `formLogin` + `logout` block. |
| `security/OAuth2SuccessHandler.java` | Issue JWT sau user sync, set cookie, redirect FE. |
| `filter/LoginRateLimitFilter.java` | `getClientIp` `private` → `public`. |
| `src/main/resources/application.properties` | Thêm `app.security.jwt.*` config. |
| `frontend/src/services/http.ts` | Bỏ CSRF interceptor + `setCsrfToken`/`clearCsrfToken`. |
| `frontend/src/stores/auth.store.ts` | Bỏ `http.clearCsrfToken()` call trong `clear()`. |
| `frontend/index.html` | Bỏ 2 meta tag `_csrf`/`_csrf_header`. |

### Test files mới (4)

| File | Test cho |
|---|---|
| `src/test/java/.../security/JwtServiceTest.java` | generate/parse/validate/expired/blacklist |
| `src/test/java/.../security/TokenBlacklistTest.java` | blacklist/isBlacklisted/cleanup |
| `src/test/java/.../security/JwtCookieServiceTest.java` | set/clear cookie attributes |
| `src/test/java/.../controller/AuthRestControllerTest.java` | login success/fail/refresh/logout |

---

## Task 1: Rate Limit Fix (BE-only, độc lập)

**Files:**
- Modify: `src/main/java/com/example/thexuong/filter/LoginRateLimitFilter.java:141` (`getClientIp` private → public)
- Modify: `src/main/java/com/example/thexuong/controller/AuthRestController.java:48-63` (login: try-catch + record attempt)
- Test: `src/test/java/com/example/thexuong/controller/AuthRestControllerRateLimitTest.java`

**Interfaces:**
- Consumes: `LoginRateLimitFilter.recordFailedAttempt(String ip)` (public, đã có), `LoginRateLimitFilter.resetAttempts(String ip)` (đã có public), `LoginRateLimitFilter.getClientIp(HttpServletRequest)` (sẽ đổi public)
- Produces: `AuthRestController.login` record failed attempts on `AuthenticationException`, reset on success.

- [ ] **Step 1.1: Đổi `getClientIp` từ private → public trong `LoginRateLimitFilter.java`**

Tìm dòng 141:
```java
    private String getClientIp(HttpServletRequest request) {
```
Đổi thành:
```java
    public String getClientIp(HttpServletRequest request) {
```

- [ ] **Step 1.2: Inject `LoginRateLimitFilter` vào `AuthRestController`**

Trong `AuthRestController.java`, thêm field vào class (sau dòng 41 `passwordResetService`):
```java
    private final LoginRateLimitFilter loginRateLimitFilter;
```
Thêm import đầu file (sau dòng 10 `import com.example.thexuong.service.UserService;`):
```java
import com.example.thexuong.filter.LoginRateLimitFilter;
```

- [ ] **Step 1.3: Refactor `login` method — try-catch + record attempt**

Thay toàn bộ method `login` (dòng 48-63) bằng:
```java
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest) {
        String clientIp = loginRateLimitFilter.getClientIp(httpRequest);
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (org.springframework.security.core.AuthenticationException e) {
            loginRateLimitFilter.recordFailedAttempt(clientIp);
            throw e; // GlobalExceptionHandler trả 401
        }
        loginRateLimitFilter.resetAttempts(clientIp);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userService.getUserByEmailWithAddresses(request.getEmail());
        UserResponse userResponse = toUserResponse(user);

        Map<String, Object> data = new HashMap<>();
        data.put("user", userResponse);
        data.put("message", "Đăng nhập thành công");

        return ResponseEntity.ok(data);
    }
```

Thêm import nếu chưa có:
```java
import org.springframework.security.core.AuthenticationException;
```
(Không cần nếu dùng fully-qualified `org.springframework.security.core.AuthenticationException` như trên.)

- [ ] **Step 1.4: Verify build pass**

Run: `./gradlew.bat build -x test --console=plain`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 1.5: Manual verify rate limit hoạt động**

Start server: `./gradlew.bat bootRun` (chạy ở 1 terminal riêng)

Test fail 5 lần (PowerShell terminal khác):
```powershell
1..5 | ForEach-Object {
  $body = @{ email = "nonexistent@test.com"; password = "wrong" } | ConvertTo-Json
  Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method POST -Body $body -ContentType "application/json" -ErrorAction SilentlyContinue
  Write-Host "Attempt $_: done"
}
# Lần 6 phải trả 429
$body = @{ email = "nonexistent@test.com"; password = "wrong" } | ConvertTo-Json
try {
  Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method POST -Body $body -ContentType "application/json"
} catch {
  Write-Host "Status: $($_.Exception.Response.StatusCode.value__)"
}
```
Expected: lần 6 trả `429`. Dừng server (Ctrl+C).

- [ ] **Step 1.6: Commit**

```bash
git add src/main/java/com/example/thexuong/filter/LoginRateLimitFilter.java src/main/java/com/example/thexuong/controller/AuthRestController.java
git commit -m "fix(rate-limit): wire recordFailedAttempt into login flow

LoginRateLimitFilter.recordFailedAttempt() was dead code —
CustomAuthenticationFailureHandler wasn't wired (SecurityConfig dùng
.failureUrl thay vì .failureHandler). Login API đi qua controller,
exception rơi vào GlobalExceptionHandler không record.

Fix: catch AuthenticationException trong AuthRestController.login,
gọi recordFailedAttempt(ip), rethrow. Reset trên success path.
Đổi getClientIp private→public để controller gọi."
```

---

## Task 2: JWT Config Properties

**Files:**
- Modify: `src/main/resources/application.properties` (append JWT config)

**Interfaces:**
- Produces: properties `app.security.jwt.secret`, `app.security.jwt.access-ttl-seconds`, `app.security.jwt.refresh-ttl-seconds`, `app.security.jwt.cookie-domain`, `app.security.jwt.cookie-secure`, `app.security.jwt.cookie-same-site` — đọc bởi `JwtService` + `JwtCookieService` qua `@Value`.

- [ ] **Step 2.1: Append JWT config vào cuối `application.properties`**

Append vào cuối file (sau dòng 33 `server.forward-headers-strategy=framework`):
```properties

# JWT Auth (httpOnly cookie)
app.security.jwt.secret=${JWT_SECRET:dev-only-secret-key-change-in-prod-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx}
app.security.jwt.access-ttl-seconds=900
app.security.jwt.refresh-ttl-seconds=604800
app.security.jwt.cookie-domain=
app.security.jwt.cookie-secure=true
app.security.jwt.cookie-same-site=lax
```

- [ ] **Step 2.2: Verify build pass**

Run: `./gradlew.bat build -x test --console=plain`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 2.3: Commit**

```bash
git add src/main/resources/application.properties
git commit -m "feat(jwt): add app.security.jwt.* config properties

Secret qua env var JWT_SECRET (dev fallback key), TTL access 15m /
refresh 7d, cookie secure + same-site=lax."
```

---

## Task 3: TokenBlacklist (in-memory jti revoke)

**Files:**
- Create: `src/main/java/com/example/thexuong/security/TokenBlacklist.java`
- Test: `src/test/java/com/example/thexuong/security/TokenBlacklistTest.java`

**Interfaces:**
- Consumes: `java.time.Instant`, `org.springframework.stereotype.Component`, `org.springframework.scheduling.annotation.Scheduled` (`@EnableScheduling` đã có ở `TheXuongApplication.java:12`)
- Produces: `TokenBlacklist.blacklist(String jti, Instant exp)`, `boolean isBlacklisted(String jti)` — dùng bởi `JwtService.isJtiBlacklisted()` và `AuthRestController.logout`.

- [ ] **Step 3.1: Viết failing test `TokenBlacklistTest.java`**

Tạo file `src/test/java/com/example/thexuong/security/TokenBlacklistTest.java`:
```java
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
```

- [ ] **Step 3.2: Run test để verify fail**

Run: `./gradlew.bat test --tests "com.example.thexuong.security.TokenBlacklistTest" --console=plain`
Expected: FAIL — `TokenBlacklist` class không tồn tại (compile error).

- [ ] **Step 3.3: Tạo `TokenBlacklist.java`**

Tạo file `src/main/java/com/example/thexuong/security/TokenBlacklist.java`:
```java
package com.example.thexuong.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory jti blacklist cho JWT revocation (logout).
 * ponytail: global map OK cho single instance; per-user map nếu throughput matters.
 * Mất khi restart — chấp nhận, window = access TTL 15m.
 */
@Component
public class TokenBlacklist {

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

    @Scheduled(fixedDelay = 300_000) // 5 phút
    public void cleanup() {
        Instant now = Instant.now();
        revokedJtis.entrySet().removeIf(e -> now.isAfter(e.getValue()));
    }
}
```

- [ ] **Step 3.4: Run test để verify pass**

Run: `./gradlew.bat test --tests "com.example.thexuong.security.TokenBlacklistTest" --console=plain`
Expected: `BUILD SUCCESSFUL`, 4 tests passed.

- [ ] **Step 3.5: Commit**

```bash
git add src/main/java/com/example/thexuong/security/TokenBlacklist.java src/test/java/com/example/thexuong/security/TokenBlacklistTest.java
git commit -m "feat(jwt): add in-memory TokenBlacklist for jti revocation

ConcurrentHashMap jti→exp, @Scheduled cleanup 5ph. Mất khi restart,
window = access TTL 15m (chấp nhận per no-DB constraint)."
```

---

## Task 4: JwtService (generate/parse/validate)

**Files:**
- Create: `src/main/java/com/example/thexuong/security/JwtService.java`
- Test: `src/test/java/com/example/thexuong/security/JwtServiceTest.java`

**Interfaces:**
- Consumes: `TokenBlacklist.isBlacklisted(jti)`, `UserDetails` (Spring Security), jjwt 0.11.5 (`io.jsonwebtoken.*`), `@Value` config từ Task 2.
- Produces:
  - `String generateAccessToken(UserDetails user)` — claims: sub=email, uid, role, type=access, iat, exp, jti=UUID; HS256; TTL từ `app.security.jwt.access-ttl-seconds`.
  - `String generateRefreshToken(UserDetails user)` — claims: sub, uid, type=refresh, iat, exp, jti; TTL từ `app.security.jwt.refresh-ttl-seconds`.
  - `Claims extractClaims(String token)`
  - `String extractUsername(String token)` — `claims.getSubject()`
  - `boolean isAccessToken(String token)` — `"access".equals(claims.get("type"))`
  - `boolean isRefreshToken(String token)` — `"refresh".equals(claims.get("type"))`
  - `boolean isExpired(String token)` — true nếu exp quá now, hoặc token invalid.
  - `boolean isJtiBlacklisted(String token)` — extract jti, check `TokenBlacklist`.
  - `boolean isValid(String token)` — parse OK + not expired + not blacklisted.

- [ ] **Step 4.1: Viết failing test `JwtServiceTest.java`**

Tạo file `src/test/java/com/example/thexuong/security/JwtServiceTest.java`:
```java
package com.example.thexuong.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
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
        blacklist.blacklist(claims.getId(), Instant.ofEpochSecond(claims.getExpiration().getEpochSecond()));
        assertThat(jwtService.isValid(token)).isFalse();
    }

    @Test
    void extractUsername_returnsSubject() {
        String token = jwtService.generateAccessToken(userDetails);
        assertThat(jwtService.extractUsername(token)).isEqualTo("user@test.com");
    }
}
```

- [ ] **Step 4.2: Run test để verify fail**

Run: `./gradlew.bat test --tests "com.example.thexuong.security.JwtServiceTest" --console=plain`
Expected: FAIL — `JwtService` class không tồn tại.

- [ ] **Step 4.3: Tạo `JwtService.java`**

Tạo file `src/main/java/com/example/thexuong/security/JwtService.java`:
```java
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
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class JwtService {

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
            secret = "dev-only-secret-key-change-in-prod-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
        this.blacklist = blacklist;
    }

    public String generateAccessToken(UserDetails user) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessTtlSeconds * 1000);
        String role = user.getAuthorities().stream().findFirst().map(GrantedAuthority::getAuthority).orElse("CUSTOMER");
        Long uid = null; // uid claim không có sẵn từ UserDetails — set trong AuthRestController trước khi gọi nếu cần
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("type", "access")
                .claim("role", role)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(UserDetails user) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshTtlSeconds * 1000);
        String role = user.getAuthorities().stream().findFirst().map(GrantedAuthority::getAuthority).orElse("CUSTOMER");
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("type", "refresh")
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
            extractClaims(token); // throws nếu invalid signature/format
            return !isExpired(token) && !isJtiBlacklisted(token);
        } catch (Exception e) {
            return false;
        }
    }
}
```

- [ ] **Step 4.4: Run test để verify pass**

Run: `./gradlew.bat test --tests "com.example.thexuong.security.JwtServiceTest" --console=plain`
Expected: `BUILD SUCCESSFUL`, 6 tests passed.

- [ ] **Step 4.5: Commit**

```bash
git add src/main/java/com/example/thexuong/security/JwtService.java src/test/java/com/example/thexuong/security/JwtServiceTest.java
git commit -m "feat(jwt): add JwtService — generate/parse/validate access+refresh

HS256, jjwt 0.11.5 (dep đã có). Claims: sub, type, role, jti, iat, exp.
isValid = parse OK + not expired + not blacklisted. Secret từ env var
JWT_SECRET, fallback dev key + log warning."
```

---

## Task 5: JwtCookieService (set/clear httpOnly cookie)

**Files:**
- Create: `src/main/java/com/example/thexuong/security/JwtCookieService.java`
- Test: `src/test/java/com/example/thexuong/security/JwtCookieServiceTest.java`

**Interfaces:**
- Consumes: `jakarta.servlet.http.HttpServletResponse`, `jakarta.servlet.http.Cookie`, `@Value` config.
- Produces:
  - `void setAuthCookies(HttpServletResponse res, String accessToken, String refreshToken)` — set 2 cookie `access_token` + `refresh_token`, httpOnly, secure, path=/, sameSite=Lax, maxAge theo TTL.
  - `void clearAuthCookies(HttpServletResponse res)` — set 2 cookie Max-Age=0.
  - `String readCookie(HttpServletRequest req, String name)` — đọc cookie value theo tên, null nếu không có.

- [ ] **Step 5.1: Viết failing test `JwtCookieServiceTest.java`**

Tạo file `src/test/java/com/example/thexuong/security/JwtCookieServiceTest.java`:
```java
package com.example.thexuong.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class JwtCookieServiceTest {

    private JwtCookieService cookieService;

    @BeforeEach
    void setUp() {
        cookieService = new JwtCookieService("", true, "lax");
    }

    @Test
    void setAuthCookies_addsBothCookies() {
        MockHttpServletResponse res = new MockHttpServletResponse();
        cookieService.setAuthCookies(res, "access123", "refresh456");
        String setCookie = res.getHeader("Set-Cookie");
        assertThat(setCookie).isNotNull();
        // Spring MockHttpServletResponse gộp nhiều Set-Cookie header, kiểm tra contain cả 2
        assertThat(res.getCookies()).anySatisfy(c -> {
            assertThat(c.getName()).isEqualTo("access_token");
            assertThat(c.getValue()).isEqualTo("access123");
            assertThat(c.isHttpOnly()).isTrue();
            assertThat(c.getSecure()).isTrue();
            assertThat(c.getPath()).isEqualTo("/");
        });
        assertThat(res.getCookies()).anySatisfy(c -> {
            assertThat(c.getName()).isEqualTo("refresh_token");
            assertThat(c.getValue()).isEqualTo("refresh456");
        });
    }

    @Test
    void clearAuthCookies_setsMaxAgeZero() {
        MockHttpServletResponse res = new MockHttpServletResponse();
        cookieService.clearAuthCookies(res);
        assertThat(res.getCookies()).allSatisfy(c -> {
            assertThat(c.getMaxAge()).isEqualTo(0);
            assertThat(c.getValue()).isEmpty();
        });
    }

    @Test
    void readCookie_returnsValue_whenPresent() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new Cookie("access_token", "tok123"));
        assertThat(cookieService.readCookie(req, "access_token")).isEqualTo("tok123");
    }

    @Test
    void readCookie_returnsNull_whenAbsent() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        assertThat(cookieService.readCookie(req, "access_token")).isNull();
    }
}
```

- [ ] **Step 5.2: Run test để verify fail**

Run: `./gradlew.bat test --tests "com.example.thexuong.security.JwtCookieServiceTest" --console=plain`
Expected: FAIL — `JwtCookieService` không tồn tại.

- [ ] **Step 5.3: Tạo `JwtCookieService.java`**

Tạo file `src/main/java/com/example/thexuong/security/JwtCookieService.java`:
```java
package com.example.thexuong.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtCookieService {

    private final String domain;
    private final boolean secure;
    private final String sameSite;

    public JwtCookieService(@Value("${app.security.jwt.cookie-domain:}") String domain,
                            @Value("${app.security.jwt.cookie-secure:true}") boolean secure,
                            @Value("${app.security.jwt.cookie-same-site:lax}") String sameSite) {
        this.domain = domain;
        this.secure = secure;
        this.sameSite = sameSite;
    }

    public void setAuthCookies(HttpServletResponse res, String accessToken, String refreshToken) {
        res.addHeader("Set-Cookie", buildCookie("access_token", accessToken, 900));
        res.addHeader("Set-Cookie", buildCookie("refresh_token", refreshToken, 604800));
    }

    public void clearAuthCookies(HttpServletResponse res) {
        res.addHeader("Set-Cookie", buildCookie("access_token", "", 0));
        res.addHeader("Set-Cookie", buildCookie("refresh_token", "", 0));
    }

    public String readCookie(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }

    private String buildCookie(String name, String value, long maxAgeSeconds) {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("=").append(value);
        sb.append("; Path=/");
        sb.append("; HttpOnly");
        if (secure) sb.append("; Secure");
        sb.append("; SameSite=").append(sameSite);
        if (maxAgeSeconds >= 0) sb.append("; Max-Age=").append(maxAgeSeconds);
        if (domain != null && !domain.isBlank()) sb.append("; Domain=").append(domain);
        return sb.toString();
    }
}
```

Note: dùng `Set-Cookie` header trực tiếp (thay vì `response.addCookie`) để set được `SameSite` attribute — `jakarta.servlet.http.Cookie` không hỗ trợ SameSite API.

- [ ] **Step 5.4: Run test để verify pass**

Run: `./gradlew.bat test --tests "com.example.thexuong.security.JwtCookieServiceTest" --console=plain`
Expected: `BUILD SUCCESSFUL`, 4 tests passed.

- [ ] **Step 5.5: Commit**

```bash
git add src/main/java/com/example/thexuong/security/JwtCookieService.java src/test/java/com/example/thexuong/security/JwtCookieServiceTest.java
git commit -m "feat(jwt): add JwtCookieService — set/clear httpOnly cookies

Dùng Set-Cookie header trực tiếp (Cookie API không hỗ trợ SameSite).
access_token + refresh_token: HttpOnly, Secure, Path=/, SameSite=Lax."
```

---

## Task 6: JwtAuthenticationFilter (read cookie, set SecurityContext, auto-refresh)

**Files:**
- Create: `src/main/java/com/example/thexuong/security/JwtAuthenticationFilter.java`
- Test: `src/test/java/com/example/thexuong/security/JwtAuthenticationFilterTest.java`

**Interfaces:**
- Consumes: `JwtService`, `JwtCookieService`, `UserDetailsService` (bean từ ApplicationConfig), `TokenBlacklist`.
- Produces: Spring Security filter bean, tự wire vào chain qua `addFilterBefore` (Task 7). Skip path permitAll (xem SecurityConfig:168-172).

- [ ] **Step 6.1: Viết failing test `JwtAuthenticationFilterTest.java`**

Tạo file `src/test/java/com/example/thexuong/security/JwtAuthenticationFilterTest.java`:
```java
package com.example.thexuong.security;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private JwtCookieService cookieService;
    private UserDetailsService userDetailsService;
    private JwtAuthenticationFilter filter;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        TokenBlacklist blacklist = new TokenBlacklist();
        jwtService = new JwtService("test-secret-key-at-least-256-bits-long-xxxxxxxxxxxxxxxxxxxx", 900L, 604800L, blacklist);
        cookieService = new JwtCookieService("", true, "lax");
        userDetailsService = mock(UserDetailsService.class);
        filter = new JwtAuthenticationFilter(jwtService, cookieService, userDetailsService);
        userDetails = User.withUsername("user@test.com")
                .password("dummy")
                .authorities(List.of(() -> "CUSTOMER"))
                .build();
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_validAccessToken_setsAuthentication() throws Exception {
        String token = jwtService.generateAccessToken(userDetails);
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new Cookie("access_token", token));
        req.setRequestURI("/api/v1/cart");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("user@test.com");
    }

    @Test
    void doFilter_noCookie_doesNotSetAuthentication() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/api/v1/cart");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilter_permitAllPath_skipsFilter() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/api/v1/products");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        // Không đụng SecurityContext, chain vẫn chạy
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void doFilter_garbageToken_doesNotSetAuthentication() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new Cookie("access_token", "garbage.token.here"));
        req.setRequestURI("/api/v1/cart");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
```

- [ ] **Step 6.2: Run test để verify fail**

Run: `./gradlew.bat test --tests "com.example.thexuong.security.JwtAuthenticationFilterTest" --console=plain`
Expected: FAIL — `JwtAuthenticationFilter` không tồn tại.

- [ ] **Step 6.3: Tạo `JwtAuthenticationFilter.java`**

Tạo file `src/main/java/com/example/thexuong/security/JwtAuthenticationFilter.java`:
```java
package com.example.thexuong.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final JwtCookieService cookieService;
    private final UserDetailsService userDetailsService;

    // Path permitAll — skip filter để tránh load user vô ích
    private static final List<String> SKIP_PREFIXES = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/api/v1/products",
            "/api/v1/categories",
            "/api/v1/chatbot"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return SKIP_PREFIXES.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String accessToken = cookieService.readCookie(request, "access_token");

        // Fallback: Authorization: Bearer header (cho Postman/test)
        if (accessToken == null) {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                accessToken = header.substring(7);
            }
        }

        if (accessToken != null && jwtService.isValid(accessToken) && jwtService.isAccessToken(accessToken)) {
            setAuthentication(accessToken, request);
        } else if (accessToken != null && jwtService.isExpired(accessToken)) {
            // Auto-refresh: thử refresh token cookie
            String refreshToken = cookieService.readCookie(request, "refresh_token");
            if (refreshToken != null && jwtService.isValid(refreshToken) && jwtService.isRefreshToken(refreshToken)) {
                String username = jwtService.extractUsername(refreshToken);
                try {
                    UserDetails user = userDetailsService.loadUserByUsername(username);
                    String newAccess = jwtService.generateAccessToken(user);
                    String newRefresh = jwtService.generateRefreshToken(user);
                    cookieService.setAuthCookies(response, newAccess, newRefresh);
                    setAuthentication(newAccess, request);
                    log.debug("[JWT] Auto-refreshed access token for user {}", username);
                } catch (Exception e) {
                    log.warn("[JWT] Refresh failed for user {}: {}", username, e.getMessage());
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String token, HttpServletRequest request) {
        try {
            String username = jwtService.extractUsername(token);
            UserDetails user = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    user, null, user.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception e) {
            log.warn("[JWT] setAuthentication failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
    }
}
```

- [ ] **Step 6.4: Run test để verify pass**

Run: `./gradlew.bat test --tests "com.example.thexuong.security.JwtAuthenticationFilterTest" --console=plain`
Expected: `BUILD SUCCESSFUL`, 4 tests passed.

- [ ] **Step 6.5: Commit**

```bash
git add src/main/java/com/example/thexuong/security/JwtAuthenticationFilter.java src/test/java/com/example/thexuong/security/JwtAuthenticationFilterTest.java
git commit -m "feat(jwt): add JwtAuthenticationFilter — read cookie, set context, auto-refresh

OncePerRequestFilter. Đọc access_token cookie (fallback Bearer header).
Valid → set SecurityContext. Expired → thử refresh cookie, sign access
mới, set cookie response. Skip permitAll paths (shouldNotFilter)."
```

---

## Task 7: SecurityConfig — wire JWT, STATELESS, bỏ formLogin

**Files:**
- Modify: `src/main/java/com/example/thexuong/config/SecurityConfig.java` (inject `JwtAuthenticationFilter`, sửa `filterChain`)

**Interfaces:**
- Consumes: `JwtAuthenticationFilter` bean (Task 6), `SessionCreationPolicy.STATELESS`.
- Produces: SecurityFilterChain với JWT filter trước `UsernamePasswordAuthenticationFilter`, no session, no formLogin, no logout block.

- [ ] **Step 7.1: Inject `JwtAuthenticationFilter` vào `SecurityConfig`**

Trong `SecurityConfig.java`, thêm field vào class (sau dòng 41 `private final UserRepository userRepository;`):
```java
    private final com.example.thexuong.security.JwtAuthenticationFilter jwtAuthenticationFilter;
```

- [ ] **Step 7.2: Sửa `filterChain` bean — thêm JWT filter + STATELESS + bỏ formLogin/logout**

Trong `SecurityConfig.java`, sửa method `filterChain` (dòng 149-205). Thay block từ `.authenticationProvider(authenticationProvider)` (dòng 179) đến cuối method (dòng 205) bằng:

```java
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
                .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo
                        .userService(oauth2UserService())
                )
                .successHandler(oAuth2SuccessHandler)
                );

        return http.build();
    }
```

Cụ thể: **xóa** block `.formLogin(...)` (dòng 180-188) và block `.logout(...)` (dòng 196-202). Giữ `.oauth2Login(...)` (đã sửa ở trên — bỏ `.loginPage("/login")` cũng được vì không còn Thymeleaf, nhưng giữ cho OAuth2 redirect mặc định). Giữ nguyên `.cors`, `.csrf`, `.exceptionHandling`, `.authorizeHttpRequests`, `.authenticationProvider` ở trên.

Method `filterChain` sau khi sửa (đầy đủ):
```java
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            if (!response.isCommitted()) {
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                response.setContentType("application/json");
                                response.getWriter().write("{\"error\":\"Forbidden\"}");
                            }
                        })
                )
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/forgot-password", "/api/v1/auth/reset-password").permitAll()
                .requestMatchers("/api/v1/products/**", "/api/v1/categories/**").permitAll()
                .requestMatchers("/api/v1/chatbot/**").permitAll()
                .requestMatchers("/api/v1/addresses", "/api/v1/addresses/**", "/api/v1/maps", "/api/v1/maps/**").authenticated()
                .requestMatchers("/api/v1/admin/**").hasAnyAuthority("ADMIN", "BOTH")
                .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
                .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo
                        .userService(oauth2UserService())
                )
                .successHandler(oAuth2SuccessHandler)
                );

        return http.build();
    }
```

- [ ] **Step 7.3: Verify build pass**

Run: `./gradlew.bat build -x test --console=plain`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7.4: Verify app start không error**

Run (quick, Ctrl+C sau khi thấy "Started TheXuongApplication"):
```bash
./gradlew.bat bootRun --console=plain
```
Expected: log "Started TheXuongApplication", không stacktrace. Ctrl+C.

- [ ] **Step 7.5: Commit**

```bash
git add src/main/java/com/example/thexuong/config/SecurityConfig.java
git commit -m "feat(security): wire JWT filter, STATELESS, drop formLogin/logout

addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter).
sessionCreationPolicy=STATELESS — no JSESSIONID. Bỏ formLogin (không còn
Thymeleaf /login). Bỏ logout block (API /api/v1/auth/logout đảm nhiệm).
Giữ oauth2Login (successHandler sẽ issue JWT trong task 9)."
```

---

## Task 8: AuthRestController — login issue JWT, logout blacklist, NEW /refresh

**Files:**
- Modify: `src/main/java/com/example/thexuong/controller/AuthRestController.java` (login, logout, +refresh endpoint)
- Test: `src/test/java/com/example/thexuong/controller/AuthRestControllerJwtTest.java`

**Interfaces:**
- Consumes: `JwtService`, `JwtCookieService`, `TokenBlacklist`, `UserDetailsService`, `LoginRateLimitFilter` (đã inject Task 1).
- Produces:
  - `POST /api/v1/auth/login` → set 2 cookie + return `{user, message}`.
  - `POST /api/v1/auth/logout` → blacklist 2 jti + clear 2 cookie + clear context.
  - `POST /api/v1/auth/refresh` → đọc refresh cookie, validate, issue access+refresh mới, set cookie, return `{user}`.

- [ ] **Step 8.1: Inject JWT services vào `AuthRestController`**

Trong `AuthRestController.java`, thêm 4 field (sau `loginRateLimitFilter` field từ Task 1):
```java
    private final com.example.thexuong.security.JwtService jwtService;
    private final com.example.thexuong.security.JwtCookieService jwtCookieService;
    private final com.example.thexuong.security.TokenBlacklist tokenBlacklist;
    private final org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
```

- [ ] **Step 8.2: Refactor `login` — issue JWT + set cookie**

Thay method `login` (đã sửa ở Task 1) bằng:
```java
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest,
                                   HttpServletResponse httpResponse) {
        String clientIp = loginRateLimitFilter.getClientIp(httpRequest);
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (org.springframework.security.core.AuthenticationException e) {
            loginRateLimitFilter.recordFailedAttempt(clientIp);
            throw e;
        }
        loginRateLimitFilter.resetAttempts(clientIp);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        jwtCookieService.setAuthCookies(httpResponse, accessToken, refreshToken);

        User user = userService.getUserByEmailWithAddresses(request.getEmail());
        UserResponse userResponse = toUserResponse(user);

        Map<String, Object> data = new HashMap<>();
        data.put("user", userResponse);
        data.put("message", "Đăng nhập thành công");

        return ResponseEntity.ok(data);
    }
```

Thêm import:
```java
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.servlet.http.Cookie;
```

- [ ] **Step 8.3: Refactor `logout` — blacklist jti + clear cookie**

Thay method `logout` (dòng 70-82) bằng:
```java
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // Blacklist access token jti
        String accessToken = jwtCookieService.readCookie(request, "access_token");
        if (accessToken != null) {
            try {
                var claims = jwtService.extractClaims(accessToken);
                tokenBlacklist.blacklist(claims.getId(),
                        claims.getExpiration().toInstant());
            } catch (Exception ignored) {}
        }
        // Blacklist refresh token jti
        String refreshToken = jwtCookieService.readCookie(request, "refresh_token");
        if (refreshToken != null) {
            try {
                var claims = jwtService.extractClaims(refreshToken);
                tokenBlacklist.blacklist(claims.getId(),
                        claims.getExpiration().toInstant());
            } catch (Exception ignored) {}
        }
        SecurityContextHolder.clearContext();
        jwtCookieService.clearAuthCookies(response);
        return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
    }
```

Bỏ import `jakarta.servlet.http.HttpSession` (không còn dùng), bỏ import `jakarta.servlet.http.Cookie` nếu không dùng (Cookie không còn dùng trực tiếp).

- [ ] **Step 8.4: Thêm endpoint `POST /refresh`**

Thêm method mới sau `logout`:
```java
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtCookieService.readCookie(request, "refresh_token");
        if (refreshToken == null || !jwtService.isValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Refresh token không hợp lệ"));
        }
        String email = jwtService.extractUsername(refreshToken);
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            String newAccess = jwtService.generateAccessToken(userDetails);
            String newRefresh = jwtService.generateRefreshToken(userDetails);
            jwtCookieService.setAuthCookies(response, newAccess, newRefresh);

            User user = userService.getUserByEmailWithAddresses(email);
            return ResponseEntity.ok(Map.of("user", toUserResponse(user)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Không thể refresh token"));
        }
    }
```

- [ ] **Step 8.5: Update SecurityConfig — `/api/v1/auth/refresh` permitAll**

Trong `SecurityConfig.java`, sửa dòng permitAll auth (dòng 168) thêm `/refresh`:
```java
                .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/forgot-password", "/api/v1/auth/reset-password", "/api/v1/auth/refresh").permitAll()
```

- [ ] **Step 8.6: Verify build pass**

Run: `./gradlew.bat build -x test --console=plain`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 8.7: Manual verify login flow**

Start: `./gradlew.bat bootRun` (terminal 1)

Test login (terminal 2 — PowerShell):
```powershell
$body = @{ email = "admin@thexuong.com"; password = "Sontran1903@" } | ConvertTo-Json
resp = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/auth/login" -Method POST -Body $body -ContentType "application/json" -SessionVariable session
Write-Host "Status: $($resp.StatusCode)"
Write-Host "Body: $($resp.Content)"
# Cookie có trong $session.Cookies
$session.Cookies | Format-Table Name, Value
```
Expected: Status 200, body có `user`, `$session.Cookies` có `access_token` + `refresh_token`.

Test logout:
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/v1/auth/logout" -Method POST -WebSession $session
Write-Host "Logout OK"
```

Ctrl+C terminal 1.

- [ ] **Step 8.8: Commit**

```bash
git add src/main/java/com/example/thexuong/controller/AuthRestController.java src/main/java/com/example/thexuong/config/SecurityConfig.java
git commit -m "feat(auth): login issues JWT cookies, logout blacklists jti, add /refresh

login: authenticate → generate access+refresh → set httpOnly cookie.
logout: extract jti từ 2 cookie, blacklist, clear cookie, clear context.
NEW /api/v1/auth/refresh: validate refresh cookie, issue new pair, return
{user}. SecurityConfig: /refresh permitAll."
```

---

## Task 9: OAuth2SuccessHandler — issue JWT sau Google login

**Files:**
- Modify: `src/main/java/com/example/thexuong/security/OAuth2SuccessHandler.java`

**Interfaces:**
- Consumes: `JwtService`, `JwtCookieService`, `UserRepository` (đã có), `app.frontend.url`.
- Produces: Sau Google login → sync user → issue JWT → set cookie → redirect FE.

- [ ] **Step 9.1: Inject `JwtService` + `JwtCookieService` vào `OAuth2SuccessHandler`**

Trong `OAuth2SuccessHandler.java`, thêm 2 field (sau dòng 22 `private final UserRepository userRepository;`):
```java
    private final com.example.thexuong.security.JwtService jwtService;
    private final com.example.thexuong.security.JwtCookieService jwtCookieService;
```

- [ ] **Step 9.2: Refactor `onAuthenticationSuccess` — issue JWT**

Thay toàn bộ method `onAuthenticationSuccess` (dòng 27-54) bằng:
```java
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // Sync user vào DB (nếu chưa có thì tạo mới)
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()
                    .email(email)
                    .username(email)
                    .fullName(name)
                    .password("")
                    .provider("GOOGLE")
                    .role("CUSTOMER")
                    .active(true)
                    .build();
            return userRepository.save(newUser);
        });

        // Issue JWT — load UserDetails từ email
        String role = (user.getRole() == null || user.getRole().isBlank()) ? "CUSTOMER" : user.getRole();
        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(email)
                .password("")
                .disabled(!Boolean.TRUE.equals(user.getActive()))
                .authorities(List.of(new SimpleGrantedAuthority(role)))
                .build();

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        jwtCookieService.setAuthCookies(response, accessToken, refreshToken);

        // Redirect tới FE OAuth callback page
        String redirectUrl = frontendUrl + "/oauth/callback";
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
```

Thêm import đầu file:
```java
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;
```

- [ ] **Step 9.3: Verify build pass**

Run: `./gradlew.bat build -x test --console=plain`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 9.4: Manual verify Google login (optional, cần Google OAuth config valid)**

Skip nếu không test được Google flow locally. Đảm bảo build pass là đủ.

- [ ] **Step 9.5: Commit**

```bash
git add src/main/java/com/example/thexuong/security/OAuth2SuccessHandler.java
git commit -m "feat(oauth): issue JWT cookies after Google login success

Sau user sync, build UserDetails từ User entity, generate access+refresh,
set httpOnly cookie, redirect FE /oauth/callback. Bỏ dependency session."
```

---

## Task 10: Frontend cleanup — bỏ CSRF interceptor + meta tag

**Files:**
- Modify: `frontend/src/services/http.ts` (bỏ CSRF interceptor + 2 method)
- Modify: `frontend/src/stores/auth.store.ts` (bỏ `http.clearCsrfToken()` call)
- Modify: `frontend/index.html` (bỏ 2 meta tag `_csrf`)

**Interfaces:**
- Consumes: existing axios client, auth.store.
- Produces: FE không còn gửi `X-CSRF-TOKEN` header (JWT cookie không cần CSRF, SameSite=Lax mitigation).

- [ ] **Step 10.1: Bỏ CSRF interceptor trong `http.ts`**

Trong `frontend/src/services/http.ts`, xóa block interceptor (dòng 11-18):
```typescript
client.interceptors.request.use((config) => {
  if (['POST', 'PUT', 'PATCH', 'DELETE'].includes(config.method?.toUpperCase() || '')) {
    const meta = document.querySelector('meta[name="_csrf"]')
    const token = meta?.getAttribute('content') || localStorage.getItem('csrf_token')
    if (token) config.headers['X-CSRF-TOKEN'] = token
  }
  return config
})
```

Xóa 2 method trong object `http` (dòng 39-40):
```typescript
  setCsrfToken: (token: string) => localStorage.setItem('csrf_token', token),
  clearCsrfToken: () => localStorage.removeItem('csrf_token')
```

File `http.ts` sau khi sửa (đầy đủ):
```typescript
import axios, { AxiosInstance } from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api/v1'

const client: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true
})

client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      if (error.config?.url && !error.config.url.includes('/auth/user') && window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    if (error.response?.status === 403) window.location.href = '/'
    return Promise.reject(error)
  }
)

const http = {
  get: (url: string, config?: any) => client.get(url, config),
  post: (url: string, data?: any, config?: any) => client.post(url, data, config),
  put: (url: string, data?: any, config?: any) => client.put(url, data, config),
  patch: (url: string, data?: any, config?: any) => client.patch(url, data, config),
  delete: (url: string, config?: any) => client.delete(url, config)
}

export default http
```

- [ ] **Step 10.2: Bỏ `http.clearCsrfToken()` call trong `auth.store.ts`**

Trong `frontend/src/stores/auth.store.ts`, trong method `clear()` (dòng 96-104), xóa dòng 101:
```typescript
      http.clearCsrfToken()
```

Cũng bỏ import `http` nếu không còn dùng ở đâu khác trong file. Kiểm tra: `http` chỉ dùng ở dòng 101 → bỏ luôn import dòng 4:
```typescript
import http from '@/services/http'
```

- [ ] **Step 10.3: Bỏ 2 meta tag CSRF trong `index.html`**

Trong `frontend/index.html`, xóa dòng 8-10:
```html
    <!-- CSRF Token will be injected by Spring Boot -->
    <meta name="_csrf" content="">
    <meta name="_csrf_header" content="X-CSRF-TOKEN">
```

- [ ] **Step 10.4: Verify FE build pass**

Run:
```powershell
cd frontend; npm run build
```
Expected: `✓ built in <s>`, no errors.

- [ ] **Step 10.5: Commit**

```bash
git add frontend/src/services/http.ts frontend/src/stores/auth.store.ts frontend/index.html
git commit -m "chore(fe): drop CSRF interceptor + meta tags (JWT cookie auth)

JWT trong httpOnly cookie + SameSite=Lax → CSRF không còn cần. Bỏ
interceptor X-CSRF-TOKEN, setCsrfToken/clearCsrfToken methods, 2 meta
tag _csrf/_csrf_header trong index.html."
```

---

## Task 11: E2E verify + full build

**Files:** None (verification only)

- [ ] **Step 11.1: Full build (BE + FE)**

Run: `./gradlew.bat build -x test --console=plain`
Expected: `BUILD SUCCESSFUL` (FE build + BE compile + bootJar).

- [ ] **Step 11.2: Run all tests**

Run: `./gradlew.bat test --console=plain`
Expected: All tests pass (TokenBlacklist, JwtService, JwtCookieService, JwtAuthenticationFilter).

- [ ] **Step 11.3: Start server + E2E manual**

Start: `./gradlew.bat bootRun` (terminal 1)

Test flow (terminal 2 — PowerShell):
```powershell
# 1. Login sai 5 lần → 429
1..6 | ForEach-Object {
  $body = @{ email = "x@y.z"; password = "wrong" } | ConvertTo-Json
  try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method POST -Body $body -ContentType "application/json"
  } catch {
    Write-Host "Attempt ${_}: $($_.Exception.Response.StatusCode.value__)"
  }
}
# Kỳ vọng: 5 × 401, lần 6 × 429

# 2. Login đúng → cookie + user
$body = @{ email = "admin@thexuong.com"; password = "Sontran1903@" } | ConvertTo-Json
$resp = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/auth/login" -Method POST -Body $body -ContentType "application/json" -SessionVariable s
Write-Host "Login: $($resp.StatusCode)"
$s.Cookies | Format-Table Name, Value

# 3. Access protected endpoint với cookie
$me = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/user" -Method GET -WebSession $s
Write-Host "User: $($me.user.email)"

# 4. Logout → cookie cleared, token blacklisted
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/logout" -Method POST -WebSession $s

# 5. Access protected sau logout → 401 (token blacklist)
try {
  Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/user" -Method GET -WebSession $s
} catch {
  Write-Host "After logout: $($_.Exception.Response.StatusCode.value__)"
}
# Kỳ vọng: 401
```

Ctrl+C terminal 1.

- [ ] **Step 11.4: Commit final (nếu có thay đổi verify-related)**

Nếu không có thay đổi code, skip. Nếu có fix nhỏ từ verification, commit với message `fix: <mô tả>`.

---

## Self-Review (post-write)

**Spec coverage:**
- ✅ Section 1 (kiến trúc): Tasks 3-9 (JwtService, TokenBlacklist, JwtCookieService, JwtAuthenticationFilter, SecurityConfig STATELESS, AuthRestController, OAuth2SuccessHandler)
- ✅ Section 2 (component mới/sửa): 4 file mới + 5 file sửa → all covered
- ✅ Section 3 (FE thay đổi tối thiểu): Task 10
- ✅ Section 4 (Rate limit fix): Task 1
- ✅ Section 5 (thứ tự triển khai): Tasks 1→11 theo đúng phase
- ✅ Section 6 (default ponytail): env var secret (Task 2 + JwtService fallback), HS256 (Task 4), blacklist restart accept (Task 3 comment), jti UUID (Task 4), CORS không đụng, CSRF disable giữ (Task 7), n8n giữ permitAll, duplicate axios không đụng, user enumeration không đụng

**Placeholder scan:** Không có TBD/TODO. Mọi step có code hoặc command cụ thể.

**Type consistency:**
- `JwtService.generateAccessToken(UserDetails)` → dùng ở `AuthRestController.login` (Task 8), `OAuth2SuccessHandler` (Task 9), `JwtAuthenticationFilter` auto-refresh (Task 6) ✅
- `JwtCookieService.setAuthCookies(res, access, refresh)` → dùng ở Task 8, 9, 6 ✅
- `TokenBlacklist.blacklist(jti, Instant)` → dùng ở Task 8 logout ✅
- `LoginRateLimitFilter.getClientIp` public → dùng ở Task 1 ✅
- `UserDetailsService` bean (ApplicationConfig) → inject vào `JwtAuthenticationFilter` (Task 6) + `AuthRestController` (Task 8) ✅

**Potential issues fixed inline:**
- `api.ts` không tồn tại (chỉ `http.ts`) → plan chỉ sửa `http.ts` ✅
- `@EnableScheduling` đã có ở `TheXuongApplication.java:12` → không cần thêm ✅
- `UserRepository.findByEmail` đã có (dòng 21) → `OAuth2SuccessHandler` reuse ✅
- jjwt 0.11.5 dùng `Jwts.parserBuilder()` + `setSigningKey(key)` + `parseClaimsJws()` (API 0.11.x, không phải 0.12.x `parser()`) ✅
- `SameSite` cookie: `jakarta.servlet.http.Cookie` không hỗ trợ → dùng `Set-Cookie` header trực tiếp (Task 5) ✅
- Test dùng `spring-boot-starter-test` (đã có, build.gradle:85) — JUnit 5 + AssertJ + Mockito + MockWeb ✅

Plan complete.
